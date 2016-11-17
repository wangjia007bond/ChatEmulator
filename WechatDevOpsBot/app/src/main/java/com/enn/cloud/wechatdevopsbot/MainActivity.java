package com.enn.cloud.wechatdevopsbot;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.enn.cloud.socket.SocketTransceiver;
import com.enn.cloud.socket.TcpServer;

import java.io.DataOutputStream;

public class MainActivity extends AppCompatActivity implements OnClickListener {

    private Button bnConnect;
    private TextView txReceive;
    private EditText edData;

    private Handler handler = new Handler(Looper.getMainLooper());

    private TcpServer server = new TcpServer(7344) {

        @Override
        public void onConnect(SocketTransceiver client) {
            printInfo(client, "Connect");
            refreshUI(false);
        }

        @Override
        public void onConnectFailed() {
            System.out.println("Client Connect Failed");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Connect failed", Toast.LENGTH_SHORT).show();
                }
            });
        }

        @Override
        public void onReceive(SocketTransceiver client,final String s) {
            printInfo(client, "Send Data: " + s);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    txReceive.append(s);
                }
            });
        }

        @Override
        public void onDisconnect(SocketTransceiver client) {
            printInfo(client, "Disconnect");
            refreshUI(false);
        }

        @Override
        public void onServerStop() {
            System.out.println("------------Server Stopped------------");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            Process p = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(p.getOutputStream());
            os.writeBytes("pm grant com.enn.cloud.wechatdevopsbot android.permission.WRITE_SECURE_SETTINGS \n");
            os.writeBytes("exit\n");
            os.flush();
        } catch (Exception e) {

        }

        Settings.Secure.putString(getContentResolver(), Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES, "com.enn.cloud.wechatdevopsbot/DevOpsBot");
        Settings.Secure.putString(getContentResolver(), Settings.Secure.ACCESSIBILITY_ENABLED, "1");

        setContentView(R.layout.activity_main);

        this.findViewById(R.id.bn_send).setOnClickListener(this);
        bnConnect = (Button) this.findViewById(R.id.bn_connect);
        bnConnect.setOnClickListener(this);

        edData = (EditText) this.findViewById(R.id.ed_dat);
        txReceive = (TextView) this.findViewById(R.id.tx_receive);
        txReceive.setOnClickListener(this);

        refreshUI(false);
    }

    @Override
    public void onStop() {
        server.onDisconnect(null);
        super.onStop();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_connect:
                connect();
                break;
            case R.id.bn_send:
                sendStr();
                break;
            case R.id.tx_receive:
                clear();
                break;
        }
    }

    private void refreshUI(final boolean isConnected) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                bnConnect.setText(isConnected ? "Disconnect" : "Connect");
            }
        });
    }

    private void connect() {
        server.start();
    }

    private void sendStr() {
        try {
            String data = edData.getText().toString();
            for (SocketTransceiver socketTransceiver : server.getClients()) {
                socketTransceiver.send(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clear() {
        new AlertDialog.Builder(this).setTitle("Confirm Clear?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        txReceive.setText("");
                    }
                }).show();
    }

    static void printInfo(SocketTransceiver st, String msg) {
        if(st != null && st.getInetAddress() != null && st.getInetAddress().getHostAddress() != null && msg != null) {
            System.out.println("Client " + st.getInetAddress().getHostAddress());
            System.out.println("   " + msg);
        }
    }
}
