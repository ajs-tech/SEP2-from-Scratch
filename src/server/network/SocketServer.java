package server.network;

import server.model.ServerModel;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Main server class that listens for client connections.
 */
public class SocketServer {
    private static final int DEFAULT_PORT = 8888;

    private ServerModel serverModel;
    private ServerSocket serverSocket;
    private boolean running;
    private List<ClientHandler> connectedClients;
    private int port;

    /**
     * Creates a socket server with the default port.
     *
     * @param serverModel the server model
     */
    public SocketServer(ServerModel serverModel) {
        this(serverModel, DEFAULT_PORT);
    }

    /**
     * Creates a socket server with a specific port.
     *
     * @param serverModel the server model
     * @param port the port to listen on
     */
    public SocketServer(ServerModel serverModel, int port) {
        this.serverModel = serverModel;
        this.port = port;
        this.connectedClients = new ArrayList<>();
    }

    /**
     * Starts the server.
     *
     * @throws IOException if there's an error starting the server
     */
    public void startServer() throws IOException {
        serverSocket = new ServerSocket(port);
        running = true;

        System.out.println("Server started on port " + port);

        // Start a thread to accept clients
        new Thread(this::acceptClients).start();
    }

    /**
     * Stops the server and disconnects all clients.
     */
    public void stopServer() {
        running = false;

        // Disconnect all clients
        synchronized (connectedClients) {
            for (ClientHandler client : connectedClients) {
                client.disconnect();
            }
            connectedClients.clear();
        }

        // Close server socket
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing server socket: " + e.getMessage());
        }

        System.out.println("Server stopped");
    }

    /**
     * Continuously accepts client connections.
     */
    private void acceptClients() {
        try {
            while (running) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from " + clientSocket.getInetAddress());

                // Create handler for this client
                ClientHandler clientHandler = new ClientHandler(clientSocket, serverModel, this);

                // Add to connected clients
                synchronized (connectedClients) {
                    connectedClients.add(clientHandler);
                }

                // Start client handler thread
                clientHandler.start();
            }
        } catch (IOException e) {
            if (running) {
                System.err.println("Error accepting client: " + e.getMessage());
            }
        }
    }

    /**
     * Removes a client handler from the list of connected clients.
     *
     * @param clientHandler the client handler to remove
     */
    public void removeClient(ClientHandler clientHandler) {
        synchronized (connectedClients) {
            connectedClients.remove(clientHandler);
        }
        System.out.println("Client disconnected. Remaining clients: " + connectedClients.size());
    }

    /**
     * Gets the number of connected clients.
     *
     * @return the number of connected clients
     */
    public int getConnectedClientCount() {
        synchronized (connectedClients) {
            return connectedClients.size();
        }
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message the message to broadcast
     */
    public void broadcastToAllClients(Object message) {
        synchronized (connectedClients) {
            for (ClientHandler client : connectedClients) {
                client.sendMessage(message);
            }
        }
    }
}