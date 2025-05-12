package client.network;

import enums.PerformanceTypeEnum;
import objects.Laptop;
import objects.Reservation;
import objects.Student;
import util.Message;
import util.PropertyChangeSubjectInterface;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Implementation of the SocketClient interface.
 * Handles communication with the server.
 */
public class SocketClientImp implements SocketClient, PropertyChangeSubjectInterface, Runnable {
    // Socket and streams
    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;
    private boolean connected;
    private Thread listenerThread;
    private BlockingQueue<Message> responseQueue;

    // Event handling
    private PropertyChangeSupport support;

    // Connection info
    private String host;
    private int port;

    /**
     * Creates a new client connected to the specified host and port.
     *
     * @param host the server host
     * @param port the server port
     */
    public SocketClientImp(String host, int port) {
        this.host = host;
        this.port = port;
        this.support = new PropertyChangeSupport(this);
        this.responseQueue = new LinkedBlockingQueue<>();

        // Connect to server
        try {
            connect();
            System.out.println("Connected to server at " + host + ":" + port);
        } catch (IOException e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }
    }

    /**
     * Connects to the server.
     *
     * @throws IOException if connection fails
     */
    private void connect() throws IOException {
        try {
            // Open socket and streams
            socket = new Socket(host, port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());
            connected = true;

            // Start listener thread
            listenerThread = new Thread(this);
            listenerThread.setDaemon(true);
            listenerThread.start();

            // Register as new client
            sendMessage(new Message("new_client", null));

        } catch (IOException e) {
            connected = false;
            throw e;
        }
    }

    /**
     * Disconnects from the server.
     */
    public void disconnect() {
        if (!connected) {
            return;
        }

        try {
            connected = false;

            // Send goodbye message
            sendMessage(new Message("disconnect", null));

            // Close resources
            if (input != null) input.close();
            if (output != null) output.close();
            if (socket != null) socket.close();

            // Interrupt listener thread
            if (listenerThread != null) {
                listenerThread.interrupt();
            }

            support.firePropertyChange("connection_state", true, false);
            System.out.println("Disconnected from server");

        } catch (IOException e) {
            System.err.println("Error during disconnect: " + e.getMessage());
        }
    }

    /**
     * Sends a message to the server.
     *
     * @param message the message to send
     * @return the server's response, or null if no response or error
     */
    private synchronized Object sendMessage(Message message) {
        if (!connected) {
            try {
                connect();
            } catch (IOException e) {
                System.err.println("Failed to reconnect: " + e.getMessage());
                return null;
            }
        }

        try {
            // Clear any pending responses
            responseQueue.clear();

            // Send message
            output.writeObject(message);
            output.flush();

            // Wait for response with timeout
            try {
                Message response = responseQueue.poll(5000, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (response != null) {
                    return response.getArgs();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return null;

        } catch (IOException e) {
            System.err.println("Error sending message: " + e.getMessage());
            connected = false;
            support.firePropertyChange("connection_error", null, e.getMessage());
            return null;
        }
    }

    /**
     * Listener thread that handles incoming messages from the server.
     */
    @Override
    public void run() {
        while (connected) {
            try {
                Message message = (Message) input.readObject();

                if (message == null) {
                    continue;
                }

                String type = message.getType();

                // Handle response to a request
                if ("response".equals(type)) {
                    // Add to response queue for synchronous requests
                    responseQueue.add(message);
                    continue;
                }

                // Handle disconnect
                if ("disconnect".equals(type)) {
                    connected = false;
                    support.firePropertyChange("disconnected", null, message.getArgs());
                    break;
                }

                // For all other messages, fire property change events
                support.firePropertyChange(type, null, message);

            } catch (IOException e) {
                if (connected) {
                    System.err.println("Connection lost: " + e.getMessage());
                    connected = false;
                    support.firePropertyChange("connection_lost", null, e.getMessage());
                }
                break;
            } catch (ClassNotFoundException e) {
                System.err.println("Error reading message: " + e.getMessage());
            }
        }

        System.out.println("Listener thread exiting");
    }

    // ===== LaptopDataInterface Methods =====

    @Override
    public List<Laptop> getAllLaptops() {
        Object response = sendMessage(new Message("get_all_laptops", null));
        if (response instanceof List<?>) {
            return (List<Laptop>) response;
        }
        return new ArrayList<>();
    }

    @Override
    public List<Laptop> getAvailableLaptops() {
        Object response = sendMessage(new Message("get_available_laptops", null));
        if (response instanceof List<?>) {
            return (List<Laptop>) response;
        }
        return new ArrayList<>();
    }

    @Override
    public List<Laptop> getLoanedLaptops() {
        Object response = sendMessage(new Message("get_loaned_laptops", null));
        if (response instanceof List<?>) {
            return (List<Laptop>) response;
        }
        return new ArrayList<>();
    }

    @Override
    public Laptop seeNextAvailableLaptop(PerformanceTypeEnum performanceTypeEnum) {
        Object response = sendMessage(new Message("get_next_available_laptop", performanceTypeEnum));
        if (response instanceof Laptop) {
            return (Laptop) response;
        }
        return null;
    }

    @Override
    public Laptop getLaptopByUUID(UUID id) {
        Object response = sendMessage(new Message("get_laptop_by_uuid", id));
        if (response instanceof Laptop) {
            return (Laptop) response;
        }
        return null;
    }

    @Override
    public Laptop createLaptop(String brand, String model, int gigabyte, int ram, PerformanceTypeEnum performanceType) {
        Object[] laptopData = new Object[] { brand, model, gigabyte, ram, performanceType };
        Object response = sendMessage(new Message("create_laptop", laptopData));
        if (response instanceof Laptop) {
            return (Laptop) response;
        }
        return null;
    }

    @Override
    public Laptop updateLaptopState(UUID id) {
        Object response = sendMessage(new Message("update_laptop_state", id));
        if (response instanceof Laptop) {
            return (Laptop) response;
        }
        return null;
    }

    @Override
    public Laptop deleteLaptop(UUID id) {
        Object response = sendMessage(new Message("delete_laptop", id));
        // Response might be true/false or the deleted laptop
        if (response instanceof Laptop) {
            return (Laptop) response;
        }
        return null;
    }

    // ===== StudentDataInterface Methods =====

    @Override
    public ArrayList<Student> getAllStudents() {
        Object response = sendMessage(new Message("get_all_students", null));
        if (response instanceof List<?>) {
            return new ArrayList<>((List<Student>) response);
        }
        return new ArrayList<>();
    }

    @Override
    public int getStudentCount() {
        Object response = sendMessage(new Message("get_student_count", null));
        if (response instanceof Integer) {
            return (Integer) response;
        }
        return 0;
    }

    @Override
    public Student getStudentByID(int id) {
        Object response = sendMessage(new Message("get_student_by_id", id));
        if (response instanceof Student) {
            return (Student) response;
        }
        return null;
    }

    @Override
    public ArrayList<Student> getStudentWithHighPowerNeeds() {
        Object response = sendMessage(new Message("get_high_power_students", null));
        if (response instanceof List<?>) {
            return new ArrayList<>((List<Student>) response);
        }
        return new ArrayList<>();
    }

    @Override
    public int getStudentCountOfHighPowerNeeds() {
        Object response = sendMessage(new Message("get_high_power_count", null));
        if (response instanceof Integer) {
            return (Integer) response;
        }
        return 0;
    }

    @Override
    public ArrayList<Student> getStudentWithLowPowerNeeds() {
        Object response = sendMessage(new Message("get_low_power_students", null));
        if (response instanceof List<?>) {
            return new ArrayList<>((List<Student>) response);
        }
        return new ArrayList<>();
    }

    @Override
    public int getStudentCountOfLowPowerNeeds() {
        Object response = sendMessage(new Message("get_low_power_count", null));
        if (response instanceof Integer) {
            return (Integer) response;
        }
        return 0;
    }

    @Override
    public Student createStudent(String name, Date degreeEndDate, String degreeTitle, int viaId, String email, int phoneNumber, PerformanceTypeEnum performanceNeeded) {
        Object[] studentData = new Object[] {
                name, degreeEndDate, degreeTitle, viaId, email, phoneNumber, performanceNeeded
        };
        Object response = sendMessage(new Message("create_student", studentData));
        if (response instanceof Student) {
            return (Student) response;
        }
        return null;
    }

    @Override
    public boolean deleteStudent(int viaId) {
        Object response = sendMessage(new Message("delete_student", viaId));
        if (response instanceof Boolean) {
            return (Boolean) response;
        }
        return false;
    }

    // ===== ReservationDataInterface Methods =====

    @Override
    public Reservation createReservation(Student student, Laptop laptop) {
        Object[] reservationData = new Object[] { student, laptop };
        Object response = sendMessage(new Message("create_reservation", reservationData));
        if (response instanceof Reservation) {
            return (Reservation) response;
        }
        return null;
    }

    @Override
    public ArrayList<Student> getThoseWhoHaveLaptop() {
        Object response = sendMessage(new Message("get_students_with_laptop", null));
        if (response instanceof List<?>) {
            return new ArrayList<>((List<Student>) response);
        }
        return new ArrayList<>();
    }

    @Override
    public int getCountOfWhoHasLaptop() {
        Object response = sendMessage(new Message("get_students_with_laptop_count", null));
        if (response instanceof Integer) {
            return (Integer) response;
        }
        return 0;
    }

    // ===== Additional Methods =====

    /**
     * Gets all reservations from the server.
     *
     * @return List of all reservations
     */
    public List<Reservation> getAllReservations() {
        Object response = sendMessage(new Message("get_all_reservations", null));
        if (response instanceof List<?>) {
            return (List<Reservation>) response;
        }
        return new ArrayList<>();
    }

    /**
     * Gets all active reservations from the server.
     *
     * @return List of active reservations
     */
    public List<Reservation> getActiveReservations() {
        Object response = sendMessage(new Message("get_active_reservations", null));
        if (response instanceof List<?>) {
            return (List<Reservation>) response;
        }
        return new ArrayList<>();
    }

    /**
     * Completes a reservation (returns a laptop).
     *
     * @param reservationId The reservation UUID
     * @return True if completion succeeded, false otherwise
     */
    public boolean completeReservation(UUID reservationId) {
        Object response = sendMessage(new Message("complete_reservation", reservationId));
        if (response instanceof Boolean) {
            return (Boolean) response;
        }
        return false;
    }

    /**
     * Gets all students in the high performance queue.
     *
     * @return List of students in the high performance queue
     */
    public List<Student> getHighPerformanceQueue() {
        Object response = sendMessage(new Message("get_high_performance_queue", null));
        if (response instanceof List<?>) {
            return (List<Student>) response;
        }
        return new ArrayList<>();
    }

    /**
     * Gets all students in the low performance queue.
     *
     * @return List of students in the low performance queue
     */
    public List<Student> getLowPerformanceQueue() {
        Object response = sendMessage(new Message("get_low_performance_queue", null));
        if (response instanceof List<?>) {
            return (List<Student>) response;
        }
        return new ArrayList<>();
    }

    /**
     * Adds a student to the high performance queue.
     *
     * @param studentId The student's VIA ID
     * @return True if addition succeeded, false otherwise
     */
    public boolean addToHighPerformanceQueue(int studentId) {
        Object response = sendMessage(new Message("add_to_high_queue", studentId));
        if (response instanceof Boolean) {
            return (Boolean) response;
        }
        return false;
    }

    /**
     * Adds a student to the low performance queue.
     *
     * @param studentId The student's VIA ID
     * @return True if addition succeeded, false otherwise
     */
    public boolean addToLowPerformanceQueue(int studentId) {
        Object response = sendMessage(new Message("add_to_low_queue", studentId));
        if (response instanceof Boolean) {
            return (Boolean) response;
        }
        return false;
    }

    /**
     * Triggers queue processing on the server.
     *
     * @return The number of laptops that were assigned
     */
    public int processQueues() {
        Object response = sendMessage(new Message("process_queues", null));
        if (response instanceof Integer) {
            return (Integer) response;
        }
        return 0;
    }

    /**
     * Checks if the client is connected to the server.
     *
     * @return true if connected
     */
    public boolean isConnected() {
        return connected;
    }

    // ===== PropertyChangeSubject Methods =====

    @Override
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    @Override
    public void addListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removeListener(String propertyName, PropertyChangeListener listener) {
        support.removePropertyChangeListener(propertyName, listener);
    }
}