package server.network;

import server.model.ServerModel;
import util.Message;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

import enums.PerformanceTypeEnum;
import objects.Laptop;
import objects.Reservation;
import objects.Student;

/**
 * Handles communication with a specific client.
 */
public class ClientHandler extends Thread {
    private Socket socket;
    private ServerModel serverModel;
    private SocketServer server;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private boolean running;

    /**
     * Creates a new client handler.
     *
     * @param socket the client socket
     * @param serverModel the server model
     * @param server the socket server
     */
    public ClientHandler(Socket socket, ServerModel serverModel, SocketServer server) {
        this.socket = socket;
        this.serverModel = serverModel;
        this.server = server;
        this.running = true;
    }

    /**
     * Thread's run method - processes client messages.
     */
    @Override
    public void run() {
        try {
            // Initialize streams - output first to avoid deadlock
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            // Main message processing loop
            while (running) {
                try {
                    // Read message from client
                    Message message = (Message) input.readObject();
                    if (message == null) {
                        continue;
                    }

                    // Process message and get response
                    Object response = processMessage(message);

                    // Send response back to client
                    output.writeObject(response);
                    output.flush();
                } catch (ClassNotFoundException e) {
                    System.err.println("Error reading message: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            // Handle client disconnection
            if (running) {
                System.err.println("Error in client handler: " + e.getMessage());
            }
        } finally {
            disconnect();
        }
    }

    /**
     * Processes a message from the client and returns a response.
     *
     * @param message the message from the client
     * @return the response to send back
     */
    private Object processMessage(Message message) {
        String type = message.getType();
        Object args = message.getArgs();

        try {
            // Process message based on type
            switch (type) {
                // ===== Laptop-related messages =====
                case "get_all_laptops":
                    return serverModel.getAllLaptops();

                case "get_available_laptops":
                    return serverModel.getAvailableLaptops();

                case "get_loaned_laptops":
                    return serverModel.getLoanedLaptops();

                case "get_next_available_laptop":
                    if (args instanceof PerformanceTypeEnum) {
                        return serverModel.getNextAvailableLaptop((PerformanceTypeEnum) args);
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

                            return serverModel.createLaptop(brand, model, gigabyte, ram, performanceType);
                        }
                    }
                    break;

                case "update_laptop_state":
                    if (args instanceof UUID) {
                        return serverModel.updateLaptopState((UUID) args);
                    }
                    break;

                case "delete_laptop":
                    if (args instanceof UUID) {
                        return serverModel.deleteLaptop((UUID) args);
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

                case "get_high_power_count":
                    return serverModel.getStudentCountOfHighPowerNeeds();

                case "get_low_power_count":
                    return serverModel.getStudentCountOfLowPowerNeeds();

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

                            return serverModel.createStudent(
                                    name, degreeEndDate, degreeTitle, viaId,
                                    email, phoneNumber, performanceNeeded
                            );
                        }
                    }
                    break;

                case "delete_student":
                    if (args instanceof Integer) {
                        return serverModel.deleteStudent((Integer) args);
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
                            return serverModel.createReservation(student, laptop);
                        }
                    }
                    break;

                case "get_students_with_laptop":
                    return serverModel.getThoseWhoHaveLaptop();

                case "get_students_with_laptop_count":
                    return serverModel.getCountOfWhoHasLaptop();

                // ===== Queue-related messages =====
                case "add_to_high_queue":
                    if (args instanceof Student) {
                        serverModel.addToHighPerformanceQueue((Student) args);
                        return true;
                    }
                    break;

                case "add_to_low_queue":
                    if (args instanceof Student) {
                        serverModel.addToLowPerformanceQueue((Student) args);
                        return true;
                    }
                    break;

                case "get_high_queue_size":
                    return serverModel.getHighNeedingQueueSize();

                case "get_low_queue_size":
                    return serverModel.getLowNeedingQueueSize();

                case "get_students_in_high_queue":
                    return serverModel.getStudentsInHighPerformanceQueue();

                case "get_students_in_low_queue":
                    return serverModel.getStudentsInLowPerformanceQueue();

                default:
                    System.err.println("Unknown message type: " + type);
                    return null;
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
            return e.getMessage(); // Send error message back to client
        }

        return null;
    }

    /**
     * Sends a message to the client.
     *
     * @param message the message to send
     */
    public void sendMessage(Object message) {
        try {
            output.writeObject(message);
            output.flush();
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

        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.err.println("Error closing client socket: " + e.getMessage());
        }

        server.removeClient(this);
    }
}