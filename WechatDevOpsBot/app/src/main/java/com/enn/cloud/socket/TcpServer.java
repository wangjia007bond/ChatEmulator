package com.enn.cloud.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by wangjiahao on 24/10/2016.
 */

public abstract class TcpServer implements Runnable {

    private int port;
    private boolean runFlag;
    public List<SocketTransceiver> clients = new CopyOnWriteArrayList<>();

    public TcpServer(int port) {
        android.util.Log.d("maptrix", "the socket server port is: " + port);
        this.port = port;
    }

    public void start() {
        android.util.Log.d("maptrix", "set up socket server: " + this.toString());
        runFlag = true;
        new Thread(this).start();
    }

    @Override
    public void run() {
        System.setProperty("java.net.preferIPv4Stack", "true");
        try {
            android.util.Log.d("maptrix", "connect: " + port);
            final ServerSocket server = new ServerSocket(port);
            server.setReuseAddress(true);
            while (runFlag) {
                try {
                    final Socket socket = server.accept();
                    startClient(socket);
                } catch (IOException e) {
                    android.util.Log.d("maptrix", e.toString());
                    e.printStackTrace();
                    this.onConnectFailed();
                }
            }

            android.util.Log.d("maptrix", "server stopped");

            try {
                for(SocketTransceiver client : clients) {
                    client.stop();
                }
                clients.clear();
                server.close();
            } catch (Exception e) {
                android.util.Log.d("maptrix", e.toString());
                e.printStackTrace();
            }
        } catch (Exception e) {
            android.util.Log.d("maptrix", e.toString());
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
