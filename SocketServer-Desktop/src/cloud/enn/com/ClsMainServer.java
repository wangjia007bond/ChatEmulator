package cloud.enn.com;

/**
 * Created by wangjiahao on 13/10/2016.
 */
public class ClsMainServer {
    public static void main(String[] args) {
        int port = 1234;
        TcpServer server = new TcpServer(port) {
            @Override
            public void onConnect(SocketTransceiver client) {
                printInfo(client, "Connect");
            }

            @Override
            public void onConnectFailed() {
                System.out.println("Client Connect Failed");
            }

            @Override
            public void onReceive(SocketTransceiver client, String s) {
                printInfo(client, "Send Data: " + s);
            }

            @Override
            public void onDisconnect(SocketTransceiver client) {
                printInfo(client, "Disconnect");
            }

            @Override
            public void onServerStop() {
                System.out.println("------------Server Stopped------------");
            }
        };
        System.out.println("-------------Server Started------------");
        server.start();

        delay();
        while(true) {
            for (SocketTransceiver socketTransceiver : server.getClients()) {
                delay();
                socketTransceiver.send(": Hello from server");
            }
        }
    }

    static void delay() {
        try {
            Thread.sleep(1000);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    static void printInfo(SocketTransceiver st, String msg) {
        System.out.println("Client " + st.getInetAddress().getHostAddress());
        System.out.println("   " + msg);
    }
}
