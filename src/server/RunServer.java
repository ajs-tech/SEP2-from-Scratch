package server;

import server.model.ServerModel;
import server.model.ServerModelImpl;
import server.network.SocketServer;

/**
 * Main class for starting the Laptop Management System server.
 */
public class RunServer {

    public static void main(String[] args) {
        try {
            // Create the server model
            ServerModel serverModel = new ServerModelImpl();

            // Default port is 8888
            int port = 8888;

            // Parse command line arguments for port if provided
            if (args.length > 0) {
                try {
                    port = Integer.parseInt(args[0]);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid port number: " + args[0]);
                    System.err.println("Using default port: " + port);
                }
            }

            // Create and start the server
            SocketServer server = new SocketServer(serverModel, port);
            server.startServer();

            System.out.println("Server started on port " + port);
            System.out.println("Press Ctrl+C to stop the server");

            // Add shutdown hook to properly clean up resources when server is terminated
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Shutting down server...");
                server.stopServer();
                System.out.println("Server shut down successfully");
            }));

        } catch (Exception e) {
            System.err.println("Error starting server: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
