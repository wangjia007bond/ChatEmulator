package cloud.enn.com;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by wangjiahao on 13/10/2016.
 */
public abstract class TcpServer implements Runnable {

    private int port;
    private boolean runFlag;
    public List<SocketTransceiver> clients = new CopyOnWriteArrayList<>();

    public TcpServer(int port) {
        this.port = port;
    }

    public void start() {
        runFlag = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        try {
            final ServerSocket server = new ServerSocket(port);
            while (runFlag) {
                try {
                    final Socket socket = server.accept();
                    startClient(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                    this.onConnectFailed();
                }
            }

            try {
                for(SocketTransceiver client : clients) {
                    client.stop();
                }
                clients.clear();
                server.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        this.onServerStop();
    }

    private void startClient(final Socket socket) {
        SocketTransceiver client = new SocketTransceiver(socket) {
            @Override
            public void onReceive(InetAddress addr, String s) {
                TcpServer.this.onReceive(this, s);
            }

            @Override
            public void onDisconnect(InetAddress addr) {
                clients.remove(this);
                TcpServer.this.onDisconnect(this);
            }
        };

        client.start();
        clients.add(client);
        this.onConnect(client);
    }

    public List<SocketTransceiver> getClients() {
        return clients;
    }

    public abstract void onConnect(SocketTransceiver client);

    public abstract void onConnectFailed();

    public abstract void onReceive(SocketTransceiver client, String s);

    public abstract void onDisconnect(SocketTransceiver client);

    public abstract void onServerStop();
}
