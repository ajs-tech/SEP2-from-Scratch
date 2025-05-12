package server.network;

import util.Message;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all active client connections to the server.
 * Provides methods for adding, removing and broadcasting messages to all clients.
 */
public class ConnectionPool {
    private final List<ClientHandler> connections;

    /**
     * Creates a new ConnectionPool with an empty list of connections.
     */
    public ConnectionPool() {
        // Use a synchronized list to ensure thread safety
        connections = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * Adds a new client connection to the pool.
     *
     * @param connection The ClientHandler to add
     */
    public void addConnection(ClientHandler connection) {
        if (connection != null) {
            connections.add(connection);
            System.out.println("Client added to connection pool. Total connections: " + connections.size());
        }
    }

    /**
     * Removes a client connection from the pool.
     *
     * @param connection The ClientHandler to remove
     */
    public void removeConnection(ClientHandler connection) {
        if (connection != null) {
            connections.remove(connection);
            System.out.println("Client removed from connection pool. Remaining connections: " + connections.size());
        }
    }

    /**
     * Sends a message to all connected clients.
     *
     * @param message The message to broadcast
     */
    public void broadcastToAll(Message message) {
        System.out.println("Broadcasting message type: " + message.getType() + " to " + connections.size() + " clients");

        // Create a copy of the connection list to avoid concurrent modification issues
        List<ClientHandler> connectionsCopy;
        synchronized (connections) {
            connectionsCopy = new ArrayList<>(connections);
        }

        for (ClientHandler connection : connectionsCopy) {
            try {
                connection.sendMessage(message);
            } catch (Exception e) {
                System.err.println("Error sending broadcast to client: " + e.getMessage());
                // If sending fails, remove the client
                removeConnection(connection);
            }
        }
    }

    /**
     * Returns the number of active connections.
     *
     * @return The count of active connections
     */
    public int getConnectionCount() {
        return connections.size();
    }

    /**
     * Closes all connections in the pool.
     * Used when shutting down the server.
     */
    public void closeAllConnections() {
        System.out.println("Closing all client connections (" + connections.size() + ")");

        // Create a copy of the connection list to avoid concurrent modification issues
        List<ClientHandler> connectionsCopy;
        synchronized (connections) {
            connectionsCopy = new ArrayList<>(connections);
        }

        for (ClientHandler connection : connectionsCopy) {
            try {
                connection.disconnect();
            } catch (Exception e) {
                System.err.println("Error disconnecting client: " + e.getMessage());
            }
        }

        connections.clear();
        System.out.println("All client connections closed");
    }
}