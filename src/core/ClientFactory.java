package core;
import client.network.SocketClient;
import client.network.SocketClientImp;

public class ClientFactory {
    private static String socketHost = "localhost";
    private static int socketPort = 8888;
    private static SocketClient client;

    public static void configure(String host, int port) {
        if (client != null) {
            throw new IllegalStateException("Cannot configure after client creation");
        }
        socketHost = host;
        socketPort = port;
    }

    public static SocketClient getClient() {
        if (client == null) {
            client = new SocketClientImp(socketHost, socketPort);
        }
        return client;
    }

}
