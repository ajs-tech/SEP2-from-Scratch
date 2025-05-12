package core;

import client.network.SocketClient;
import client.network.SocketClientImp;

/**
 * Factory for creating and managing the client connection.
 * Implements the Singleton pattern to ensure only one client connection exists.
 */
public class ClientFactory {
    private static String socketHost = "localhost";
    private static int socketPort = 8888;
    private static SocketClient client;
    private static boolean initialized = false;

    /**
     * Configures the client connection settings.
     * Must be called before getClient() if non-default settings are required.
     *
     * @param host Server hostname or IP address
     * @param port Server port
     */
    public static void configure(String host, int port) {
        if (initialized) {
            throw new IllegalStateException("Cannot configure after client creation");
        }
        socketHost = host;
        socketPort = port;
    }

    /**
     * Gets the client singleton instance.
     * Creates a new instance if none exists.
     *
     * @return The SocketClient instance
     */
    public static SocketClient getClient() {
        if (client == null) {
            synchronized (ClientFactory.class) {
                if (client == null) {
                    client = new SocketClientImp(socketHost, socketPort);
                    initialized = true;
                }
            }
        }
        return client;
    }

    /**
     * Resets the client connection.
     * Useful for reconnecting or changing connection parameters.
     */
    public static void resetClient() {
        if (client != null) {
            // Ensure clean disconnect if the client implements it
            if (client instanceof SocketClientImp) {
                ((SocketClientImp) client).disconnect();
            }
            client = null;
            initialized = false;
        }
    }

    /**
     * Checks if the client is initialized.
     *
     * @return true if the client is initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }

    /**
     * Gets the configured host.
     *
     * @return the host
     */
    public static String getHost() {
        return socketHost;
    }

    /**
     * Gets the configured port.
     *
     * @return the port
     */
    public static int getPort() {
        return socketPort;
    }
}