package server.network;

import server.model.ServerModel;
import util.Message;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main server class that listens for client connections.
 * Creates and manages ClientHandler threads for connected clients.
 */
public class SocketServer {
    private static final int DEFAULT_PORT = 8888;
    private static final int THREAD_POOL_SIZE = 20; // Maximum number of concurrent clients

    private ServerModel serverModel;
    private ServerSocket serverSocket;
    private boolean running;
    private ConnectionPool connectionPool;
    private ExecutorService threadPool;
    private List<ClientHandler> activeHandlers;
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
        this.connectionPool = new ConnectionPool();
        this.activeHandlers = new ArrayList<>();
        this.threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

        // Register model listeners to broadcast changes to clients
        setupModelListeners();
    }

    /**
     * Sets up listeners for model events to broadcast changes to clients.
     */
    private void setupModelListeners() {
        // Laptop events
        serverModel.addListener("server_laptop_created", evt -> {
            connectionPool.broadcastToAll(new Message("laptop_created", evt.getNewValue()));
        });

        serverModel.addListener("server_laptop_updated", evt -> {
            connectionPool.broadcastToAll(new Message("laptop_updated", evt.getNewValue()));
        });

        serverModel.addListener("server_laptop_deleted", evt -> {
            connectionPool.broadcastToAll(new Message("laptop_deleted", evt.getNewValue()));
        });

        serverModel.addListener("server_laptop_state_changed", evt -> {
            connectionPool.broadcastToAll(new Message("laptop_state_changed", evt.getNewValue()));
        });

        // Student events
        serverModel.addListener("server_student_created", evt -> {
            connectionPool.broadcastToAll(new Message("student_created", evt.getNewValue()));
        });

        serverModel.addListener("server_student_updated", evt -> {
            connectionPool.broadcastToAll(new Message("student_updated", evt.getNewValue()));
        });

        serverModel.addListener("server_student_deleted", evt -> {
            connectionPool.broadcastToAll(new Message("student_deleted", evt.getNewValue()));
        });

        // Reservation events
        serverModel.addListener("server_reservation_created", evt -> {
            connectionPool.broadcastToAll(new Message("reservation_created", evt.getNewValue()));
        });

        serverModel.addListener("server_reservation_completed", evt -> {
            connectionPool.broadcastToAll(new Message("reservation_completed", evt.getNewValue()));
        });

        serverModel.addListener("server_reservation_cancelled", evt -> {
            connectionPool.broadcastToAll(new Message("reservation_cancelled", evt.getNewValue()));
        });

        // Queue events
        serverModel.addListener("server_queue_updated", evt -> {
            connectionPool.broadcastToAll(new Message("queue_updated", evt.getNewValue()));
        });

        // Error events
        serverModel.addListener("server_error", evt -> {
            connectionPool.broadcastToAll(new Message("server_error", evt.getNewValue()));
        });
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
        System.out.println("Waiting for client connections...");

        // Start a thread to accept clients
        new Thread(this::acceptClients).start();
    }

    /**
     * Stops the server and disconnects all clients.
     */
    public void stopServer() {
        running = false;

        // Disconnect all clients
        connectionPool.closeAllConnections();

        // Shutdown thread pool
        threadPool.shutdown();

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
        System.out.println("Client connection listener started");

        while (running) {
            try {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected from " + clientSocket.getInetAddress());

                // Create handler for this client
                ClientHandler clientHandler = new ClientHandler(clientSocket, serverModel, this, connectionPool);

                // Add to active handlers
                synchronized (activeHandlers) {
                    activeHandlers.add(clientHandler);
                }

                // Start client handler in the thread pool
                threadPool.execute(clientHandler);

            } catch (SocketException e) {
                if (!running) {
                    // Server is shutting down, this is expected
                    break;
                }
                System.err.println("Socket error: " + e.getMessage());
            } catch (IOException e) {
                if (running) {
                    System.err.println("Error accepting client: " + e.getMessage());
                }
            }
        }

        System.out.println("Client connection listener stopped");
    }

    /**
     * Removes a client handler from the list of active handlers.
     *
     * @param clientHandler the client handler to remove
     */
    public void removeClient(ClientHandler clientHandler) {
        synchronized (activeHandlers) {
            activeHandlers.remove(clientHandler);
        }
    }

    /**
     * Gets the number of connected clients.
     *
     * @return the number of connected clients
     */
    public int getConnectedClientCount() {
        return connectionPool.getConnectionCount();
    }

    /**
     * Broadcasts a message to all connected clients.
     *
     * @param message the message to broadcast
     */
    public void broadcastToAllClients(Message message) {
        connectionPool.broadcastToAll(message);
    }

    /**
     * Gets the port the server is running on.
     *
     * @return the server port
     */
    public int getPort() {
        return port;
    }

    /**
     * Checks if the server is running.
     *
     * @return true if the server is running
     */
    public boolean isRunning() {
        return running;
    }
}