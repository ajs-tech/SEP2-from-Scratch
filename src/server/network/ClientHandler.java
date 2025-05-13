package server.network;

import server.model.ServerModel;
import util.Message;
import enums.PerformanceTypeEnum;
import objects.Laptop;
import objects.Reservation;
import objects.Student;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.UUID;

/**
 * Handles communication with a specific client.
 * Runs in its own thread and processes client messages.
 */
public class ClientHandler extends Thread {
    private Socket socket;
    private ServerModel serverModel;
    private SocketServer server;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private boolean running;
    private ConnectionPool connectionPool;

    /**
     * Creates a new client handler.
     *
     * @param socket the client socket
     * @param serverModel the server model
     * @param server the socket server
     * @param connectionPool the connection pool for broadcasting
     */
    public ClientHandler(Socket socket, ServerModel serverModel, SocketServer server, ConnectionPool connectionPool) {
        this.socket = socket;
        this.serverModel = serverModel;
        this.server = server;
        this.connectionPool = connectionPool;
        this.running = true;

        try {
            // Initialize streams - output first to avoid deadlock
            this.output = new ObjectOutputStream(socket.getOutputStream());
            this.input = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.err.println("Error initializing client handler: " + e.getMessage());
            disconnect();
        }
    }

    /**
     * Thread's run method - processes client messages.
     */
    @Override
    public void run() {
        try {
            System.out.println("Client handler started for client: " + socket.getInetAddress());

            // Send welcome message to client
            sendMessage(new Message("welcome", "Connected to Laptop Management System Server"));

            // Main message processing loop
            while (running) {
                try {
                    // Read message from client
                    Message message = (Message) input.readObject();
                    if (message == null) {
                        continue;
                    }

                    System.out.println("Received message: " + message.getType());

                    // Process message and get response
                    Object response = processMessage(message);

                    // If there's a response to send back, send it
                    if (response != null) {
                        // Send response back to client
                        sendMessage(new Message("response", response));
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println("Error reading message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // Handle client disconnection
            if (running) {
                System.err.println("Client connection lost: " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }

    /**
     * Processes a message from the client and returns a response.
     *
     * @param message the message from the client
     * @return the response to send back, or null if no response needed
     */
    private Object processMessage(Message message) {
        String type = message.getType();
        Object args = message.getArgs();

        try {
            // Process message based on type
            switch (type) {
                case "new_client":
                    // Handle new client connection
                    connectionPool.addConnection(this);
                    sendInitialData();
                    return "Connected and synchronized";

                // ===== Laptop-related messages =====
                case "get_all_laptops":
                    return serverModel.getAllLaptops();

                case "get_available_laptops":
                    return serverModel.getAvailableLaptops();

                case "get_loaned_laptops":
                    return serverModel.getLoanedLaptops();

                case "get_next_available_laptop":
                    if (args instanceof PerformanceTypeEnum) {
                        return serverModel.seeNextAvailableLaptop((PerformanceTypeEnum) args);
                    }
                    break;

                case "get_laptop_by_uuid":
                    if (args instanceof UUID) {
                        return serverModel.getLaptopByUUID((UUID) args);
                    }
                    break;

                case "create_laptop":
                    if (args instanceof Object[]) {
                        Object[] laptopData = (Object[]) args;
                        if (laptopData.length == 5) {
                            String brand = (String) laptopData[0];
                            String model = (String) laptopData[1];
                            int gigabyte = (int) laptopData[2];
                            int ram = (int) laptopData[3];
                            PerformanceTypeEnum performanceType = (PerformanceTypeEnum) laptopData[4];

                            Laptop laptop = serverModel.createLaptop(brand, model, gigabyte, ram, performanceType);
                            if (laptop != null) {
                                // Broadcast to all clients
                                connectionPool.broadcastToAll(new Message("laptop_created", laptop));
                                return laptop;
                            }
                        }
                    } else if (args instanceof Laptop) {
                        // Alternative version accepting a Laptop object directly
                        Laptop laptop = (Laptop) args;
                        Laptop created = serverModel.createLaptop(
                                laptop.getBrand(),
                                laptop.getModel(),
                                laptop.getGigabyte(),
                                laptop.getRam(),
                                laptop.getPerformanceType());

                        if (created != null) {
                            // Broadcast to all clients
                            connectionPool.broadcastToAll(new Message("laptop_created", created));
                            return created;
                        }
                    }
                    break;

                case "update_laptop_state":
                    if (args instanceof UUID) {
                        Laptop laptop = serverModel.updateLaptopState((UUID) args);
                        if (laptop != null) {
                            // Broadcast to all clients
                            connectionPool.broadcastToAll(new Message("laptop_state_changed", laptop));
                            return laptop;
                        }
                    }
                    break;

                case "delete_laptop":
                    if (args instanceof UUID) {
                        Laptop laptop = serverModel.deleteLaptop((UUID) args);
                        if (laptop != null) {
                            // Broadcast to all clients
                            connectionPool.broadcastToAll(new Message("laptop_deleted", laptop));
                            return true;
                        }
                    }
                    break;

                // ===== Student-related messages =====
                case "get_all_students":
                    return serverModel.getAllStudents();

                case "get_student_count":
                    return serverModel.getStudentCount();

                case "get_student_by_id":
                    if (args instanceof Integer) {
                        return serverModel.getStudentByID((Integer) args);
                    }
                    break;

                case "get_high_power_students":
                    return serverModel.getStudentWithHighPowerNeeds();

                case "get_low_power_students":
                    return serverModel.getStudentWithLowPowerNeeds();

                case "create_student":
                    if (args instanceof Object[]) {
                        Object[] studentData = (Object[]) args;
                        if (studentData.length == 7) {
                            String name = (String) studentData[0];
                            java.util.Date degreeEndDate = (java.util.Date) studentData[1];
                            String degreeTitle = (String) studentData[2];
                            int viaId = (int) studentData[3];
                            String email = (String) studentData[4];
                            int phoneNumber = (int) studentData[5];
                            PerformanceTypeEnum performanceNeeded = (PerformanceTypeEnum) studentData[6];

                            Student student = serverModel.createStudent(
                                    name, degreeEndDate, degreeTitle, viaId,
                                    email, phoneNumber, performanceNeeded);

                            if (student != null) {
                                // Broadcast to all clients
                                connectionPool.broadcastToAll(new Message("student_created", student));
                                return student;
                            }
                        }
                    } else if (args instanceof Student) {
                        // Alternative version accepting a Student object directly
                        Student student = (Student) args;
                        Student created = serverModel.createStudent(
                                student.getName(),
                                student.getDegreeEndDate(),
                                student.getDegreeTitle(),
                                student.getViaId(),
                                student.getEmail(),
                                student.getPhoneNumber(),
                                student.getPerformanceNeeded());

                        if (created != null) {
                            // Broadcast to all clients
                            connectionPool.broadcastToAll(new Message("student_created", created));
                            return created;
                        }
                    }
                    break;

                case "delete_student":
                    if (args instanceof Integer) {
                        boolean success = serverModel.deleteStudent((Integer) args);
                        if (success) {
                            // Broadcast to all clients
                            connectionPool.broadcastToAll(new Message("student_deleted", args));
                            return true;
                        }
                    }
                    break;

                // ===== Reservation-related messages =====
                case "create_reservation":
                    if (args instanceof Object[]) {
                        Object[] reservationData = (Object[]) args;
                        if (reservationData.length == 2 &&
                                reservationData[0] instanceof Student &&
                                reservationData[1] instanceof Laptop) {

                            Student student = (Student) reservationData[0];
                            Laptop laptop = (Laptop) reservationData[1];
                            Reservation reservation = serverModel.createReservation(student, laptop);

                            if (reservation != null) {
                                // Broadcast to all clients
                                connectionPool.broadcastToAll(new Message("reservation_created", reservation));
                                return reservation;
                            }
                        }
                    }
                    break;

                case "get_active_reservations":
                    return serverModel.getActiveReservations();

                case "get_all_reservations":
                    return serverModel.getAllReservations();

                case "complete_reservation":
                    if (args instanceof UUID) {
                        boolean success = serverModel.completeReservation((UUID) args);
                        if (success) {
                            // Broadcast to all clients
                            connectionPool.broadcastToAll(new Message("reservation_completed", args));
                            return true;
                        }
                    }
                    break;

                // ===== Queue-related messages =====
                case "get_high_performance_queue":
                    return serverModel.getHighPerformanceQueue();

                case "get_low_performance_queue":
                    return serverModel.getLowPerformanceQueue();

                case "add_to_high_queue":
                    if (args instanceof Integer) {
                        boolean success = serverModel.addToHighPerformanceQueue((Integer) args);
                        if (success) {
                            // Broadcast to all clients
                            connectionPool.broadcastToAll(new Message("student_added_to_high_queue", args));
                            return true;
                        }
                    }
                    break;

                case "add_to_low_queue":
                    if (args instanceof Integer) {
                        boolean success = serverModel.addToLowPerformanceQueue((Integer) args);
                        if (success) {
                            // Broadcast to all clients
                            connectionPool.broadcastToAll(new Message("student_added_to_low_queue", args));
                            return true;
                        }
                    }
                    break;

                case "process_queues":
                    int processed = serverModel.processQueues();
                    return processed;

                case "disconnect":
                    disconnect();
                    return "Disconnected";

                default:
                    System.err.println("Unknown message type: " + type);
                    return "Unknown message type: " + type;
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
            return "Error: " + e.getMessage();
        }

        return "Message processed but no response data";
    }

    /**
     * Sends initial data to the client upon connection.
     */
    private void sendInitialData() {
        try {
            // Send all laptops
            sendMessage(new Message("all_laptops", serverModel.getAllLaptops()));

            // Send all students
            sendMessage(new Message("all_students", serverModel.getAllStudents()));

            // Send all active reservations
            sendMessage(new Message("active_reservations", serverModel.getActiveReservations()));

            // Send queue information
            sendMessage(new Message("high_performance_queue", serverModel.getHighPerformanceQueue()));
            sendMessage(new Message("low_performance_queue", serverModel.getLowPerformanceQueue()));

        } catch (Exception e) {
            System.err.println("Error sending initial data: " + e.getMessage());
        }
    }

    /**
     * Sends a message to the client.
     *
     * @param message the message to send
     */
    public void sendMessage(Message message) {
        try {
            if (output != null && message != null) {
                output.writeObject(message);
                output.flush();
            }
        } catch (IOException e) {
            System.err.println("Error sending message to client: " + e.getMessage());
            disconnect();
        }
    }

    /**
     * Disconnects the client.
     */
    public void disconnect() {
        if (!running) {
            return;
        }

        running = false;
        System.out.println("Disconnecting client: " + (socket != null ? socket.getInetAddress() : "unknown"));

        try {
            // Send goodbye message
            if (output != null) {
                sendMessage(new Message("disconnect", "Goodbye"));
            }

            // Close resources
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error during client disconnect: " + e.getMessage());
        } finally {
            // Remove from connection pool
            connectionPool.removeConnection(this);
            // Notify server
            server.removeClient(this);
        }
    }
}