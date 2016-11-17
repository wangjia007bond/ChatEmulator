package com.enn.cloud.socket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by wangjiahao on 13/10/2016.
 */

public abstract class SocketTransceiver implements Runnable {

    protected Socket socket;
    protected InetAddress addr;
    protected DataInputStream in;
    protected DataOutputStream out;
    private boolean runFlag;

    public SocketTransceiver(Socket socket) {
        android.util.Log.d("maptrix", "set up socket transceiver: " + socket.toString());
        this.socket = socket;
        this.addr = socket.getInetAddress();
    }

    public InetAddress getInetAddress() {
        return addr;
    }

    public void start() {
        android.util.Log.d("maptrix", "start this socket transceiver: " + this.toString());
        runFlag = true;
        new Thread(this).start();
    }

    public void stop() {
        android.util.Log.d("maptrix", "stop this socket transceiver: " + this.toString());
        runFlag = false;
        try {
            socket.shutdownInput();
            in.close();
        } catch(Exception e) {
            android.util.Log.d("maptrix", e.toString());
            e.printStackTrace();
        }
    }

    public boolean send(String s) {
        if(out != null) {
            try {
                android.util.Log.d("maptrix", "send from this socket transceiver: " + this.toString() + " the message is: " + s);
                out.writeUTF(s);
                out.flush();
                return true;
            } catch(Exception e) {
                android.util.Log.d("maptrix", e.toString());
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(this.socket.getInputStream());
            out = new DataOutputStream(this.socket.getOutputStream());
        } catch(IOException e) {
            android.util.Log.d("maptrix", e.toString());
            e.printStackTrace();
            runFlag = false;
        }

        while(runFlag) {
            try {
                final String s = in.readUTF();
                android.util.Log.d("maptrix", "receive from this socket transceiver: " + this.toString() + " the message is: " + s);
                this.onReceive(addr, s);
            } catch (IOException e) {
                android.util.Log.d("maptrix", e.toString());
                runFlag = false;
            }
        }

        try {
            in.close();
            out.close();
            socket.close();
            in = null;
            out = null;
            socket = null;
        } catch (IOException e) {
            android.util.Log.d("maptrix", e.toString());
            e.printStackTrace();
        }
    }

    public abstract void onReceive(InetAddress addr, String s);

    public abstract void onDisconnect(InetAddress addr);

}
