package model;

import client.network.SocketClient;
import core.ClientFactory;
import enums.PerformanceTypeEnum;
import enums.ReservationStatusEnum;
import model.helpToLogic.*;
import objects.Laptop;
import objects.Reservation;
import objects.Student;
import util.PropertyChangeSubjectInterface;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class ModelImpl implements Model {
    // Event constants for Model's own events - these are exposed to ViewModels
    public static final String EVENT_LAPTOP_CREATED = "model_laptop_created";
    public static final String EVENT_LAPTOP_DELETED = "model_laptop_deleted";
    public static final String EVENT_LAPTOP_UPDATED = "model_laptop_updated";
    public static final String EVENT_LAPTOP_STATE_CHANGED = "model_laptop_state_changed";
    public static final String EVENT_AVAILABLE_COUNT_CHANGED = "model_available_count_changed";
    public static final String EVENT_LOANED_COUNT_CHANGED = "model_loaned_count_changed";

    public static final String EVENT_STUDENT_CREATED = "model_student_created";
    public static final String EVENT_STUDENT_DELETED = "model_student_deleted";
    public static final String EVENT_STUDENT_UPDATED = "model_student_updated";

    public static final String EVENT_RESERVATION_CREATED = "model_reservation_created";
    public static final String EVENT_RESERVATION_UPDATED = "model_reservation_updated";
    public static final String EVENT_RESERVATION_COMPLETED = "model_reservation_completed";
    public static final String EVENT_RESERVATION_CANCELLED = "model_reservation_cancelled";
    public static final String EVENT_ACTIVE_RESERVATIONS_CHANGED = "model_active_reservations_changed";

    // Class variables
    private static ModelImpl INSTANCE;
    private LaptopData laptopData;
    private StudentData studentData;
    private ReservationData reservationData;
    private PropertyChangeSupport support;
    private SocketClient client;

    private ModelImpl() {
        // Initialize PropertyChangeSupport
        this.support = new PropertyChangeSupport(this);

        // Initialize data classes
        laptopData = new LaptopData();
        studentData = new StudentData();
        reservationData = new ReservationData();
        client = ClientFactory.getClient();

        // Register as listener to all data classes
        laptopData.addListener(this);
        studentData.addListener(this);
        reservationData.addListener(this);

        // Register as listener to client events
        client.addListener(this);

        // Connect data sources
        connectDataSources();
    }

    // Singleton pattern implementation
    public static ModelImpl getInstance() {
        if (INSTANCE == null) {
            synchronized (ModelImpl.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ModelImpl();
                }
            }
        }
        return INSTANCE;
    }

    // Connect components together for event flow
    private void connectDataSources() {
        // Connect LaptopData to ReservationData
        // This allows ReservationData to respond to laptop availability changes
        laptopData.addListener(LaptopData.EVENT_LAPTOP_BECAME_AVAILABLE, reservationData);
    }

    /**
     * Returns the SocketClient instance used by this model.
     * @return The client instance
     */
    @Override
    public SocketClient getClient() {
        return client;
    }

    // Property change listener implementation
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        Object source = evt.getSource();

        // Handle events from LaptopData
        if (source == laptopData) {
            handleLaptopDataEvents(propertyName, evt);
        }
        // Handle events from StudentData
        else if (source == studentData) {
            handleStudentDataEvents(propertyName, evt);
        }
        // Handle events from ReservationData
        else if (source == reservationData) {
            handleReservationDataEvents(propertyName, evt);
        }
        // Handle events from client
        else if (source == client) {
            handleClientEvents(propertyName, evt);
        }
    }

    /**
     * Handle events from the client.
     */
    private void handleClientEvents(String propertyName, PropertyChangeEvent evt) {
        // Forward relevant events from client
        if (propertyName.equals("laptop_created")) {
            support.firePropertyChange(EVENT_LAPTOP_CREATED, null, evt.getNewValue());
        }
        else if (propertyName.equals("laptop_deleted")) {
            support.firePropertyChange(EVENT_LAPTOP_DELETED, null, evt.getNewValue());
        }
        else if (propertyName.equals("laptop_updated")) {
            support.firePropertyChange(EVENT_LAPTOP_UPDATED, null, evt.getNewValue());
        }
        else if (propertyName.equals("laptop_state_changed")) {
            support.firePropertyChange(EVENT_LAPTOP_STATE_CHANGED, null, evt.getNewValue());
        }
        else if (propertyName.equals("student_created")) {
            support.firePropertyChange(EVENT_STUDENT_CREATED, null, evt.getNewValue());
        }
        else if (propertyName.equals("student_deleted")) {
            support.firePropertyChange(EVENT_STUDENT_DELETED, null, evt.getNewValue());
        }
        else if (propertyName.equals("student_updated")) {
            support.firePropertyChange(EVENT_STUDENT_UPDATED, null, evt.getNewValue());
        }
        else if (propertyName.equals("reservation_created")) {
            support.firePropertyChange(EVENT_RESERVATION_CREATED, null, evt.getNewValue());
        }
        else if (propertyName.equals("reservation_completed")) {
            support.firePropertyChange(EVENT_RESERVATION_COMPLETED, null, evt.getNewValue());
        }
        else if (propertyName.equals("reservation_cancelled")) {
            support.firePropertyChange(EVENT_RESERVATION_CANCELLED, null, evt.getNewValue());
        }
    }

    // Handle events from LaptopData
    private void handleLaptopDataEvents(String propertyName, PropertyChangeEvent evt) {
        // Forward count changes for UI updates
        if (LaptopData.EVENT_AVAILABLE_COUNT_CHANGED.equals(propertyName)) {
            support.firePropertyChange(EVENT_AVAILABLE_COUNT_CHANGED,
                    evt.getOldValue(),
                    evt.getNewValue());
        }
        else if (LaptopData.EVENT_LOANED_COUNT_CHANGED.equals(propertyName)) {
            support.firePropertyChange(EVENT_LOANED_COUNT_CHANGED,
                    evt.getOldValue(),
                    evt.getNewValue());
        }
        else if (LaptopData.EVENT_LAPTOP_STATE_CHANGED.equals(propertyName)) {
            // Forward state changes for UI updates
            support.firePropertyChange(EVENT_LAPTOP_STATE_CHANGED,
                    evt.getOldValue(),
                    evt.getNewValue());
        }
        else if (LaptopData.EVENT_LAPTOP_ADDED.equals(propertyName)) {
            // Forward laptop creation events
            support.firePropertyChange(EVENT_LAPTOP_CREATED,
                    null,
                    evt.getNewValue());
        }
        else if (LaptopData.EVENT_LAPTOP_REMOVED.equals(propertyName)) {
            // Forward laptop deletion events
            support.firePropertyChange(EVENT_LAPTOP_DELETED,
                    evt.getOldValue(),
                    null);
        }
        else if (LaptopData.EVENT_LAPTOP_UPDATED.equals(propertyName)) {
            // Forward laptop update events
            support.firePropertyChange(EVENT_LAPTOP_UPDATED,
                    evt.getOldValue(),
                    evt.getNewValue());
        }
    }

    // Handle events from StudentData
    private void handleStudentDataEvents(String propertyName, PropertyChangeEvent evt) {
        if (StudentData.EVENT_STUDENT_ADDED.equals(propertyName)) {
            // Forward student creation events
            support.firePropertyChange(EVENT_STUDENT_CREATED,
                    null,
                    evt.getNewValue());
        }
        else if (StudentData.EVENT_STUDENT_REMOVED.equals(propertyName)) {
            // Forward student deletion events
            support.firePropertyChange(EVENT_STUDENT_DELETED,
                    evt.getOldValue(),
                    null);
        }
        else if (StudentData.EVENT_STUDENT_UPDATED.equals(propertyName)) {
            // Forward student update events
            support.firePropertyChange(EVENT_STUDENT_UPDATED,
                    evt.getOldValue(),
                    evt.getNewValue());
        }
        // Handle other student data events as needed
    }

    // Handle events from ReservationData
    private void handleReservationDataEvents(String propertyName, PropertyChangeEvent evt) {
        if (ReservationData.EVENT_RESERVATION_ADDED.equals(propertyName)) {
            // Forward reservation creation events
            support.firePropertyChange(EVENT_RESERVATION_CREATED,
                    null,
                    evt.getNewValue());
        }
        else if (ReservationData.EVENT_RESERVATION_REMOVED.equals(propertyName)) {
            // Forward reservation removal events (doesn't necessarily mean completed or cancelled)
            support.firePropertyChange(EVENT_RESERVATION_UPDATED,
                    evt.getOldValue(),
                    evt.getNewValue());
        }
        else if (ReservationData.EVENT_RESERVATION_COMPLETED.equals(propertyName)) {
            // Forward reservation completion events
            support.firePropertyChange(EVENT_RESERVATION_COMPLETED,
                    null,
                    evt.getNewValue());
        }
        else if (ReservationData.EVENT_RESERVATION_CANCELLED.equals(propertyName)) {
            // Forward reservation cancellation events
            support.firePropertyChange(EVENT_RESERVATION_CANCELLED,
                    null,
                    evt.getNewValue());
        }
        else if (ReservationData.EVENT_ACTIVE_COUNT_CHANGED.equals(propertyName)) {
            // Forward active reservations count change
            support.firePropertyChange(EVENT_ACTIVE_RESERVATIONS_CHANGED,
                    evt.getOldValue(),
                    evt.getNewValue());
        }
    }

    // LaptopDataInterface methods

    @Override
    public List<Laptop> getAllLaptops() {
        return client.getAllLaptops();
    }

    @Override
    public List<Laptop> getAvailableLaptops() {
        return client.getAvailableLaptops();
    }

    @Override
    public List<Laptop> getLoanedLaptops() {
        return client.getLoanedLaptops();
    }

    @Override
    public Laptop seeNextAvailableLaptop(PerformanceTypeEnum performanceTypeEnum) {
        return client.seeNextAvailableLaptop(performanceTypeEnum);
    }

    @Override
    public Laptop getLaptopByUUID(UUID id) {
        return client.getLaptopByUUID(id);
    }

    @Override
    public Laptop createLaptop(String brand, String model, int gigabyte, int ram, PerformanceTypeEnum performanceType) {
        return client.createLaptop(brand, model, gigabyte, ram, performanceType);
    }

    @Override
    public Laptop updateLaptopState(UUID id) {
        return client.updateLaptopState(id);
    }

    @Override
    public Laptop deleteLaptop(UUID id) {
        return client.deleteLaptop(id);
    }

    // StudentDataInterface methods

    @Override
    public ArrayList<Student> getAllStudents() {
        return client.getAllStudents();
    }

    @Override
    public int getStudentCount() {
        return client.getStudentCount();
    }

    @Override
    public Student getStudentByID(int id) {
        return client.getStudentByID(id);
    }

    @Override
    public ArrayList<Student> getStudentWithHighPowerNeeds() {
        return client.getStudentWithHighPowerNeeds();
    }

    @Override
    public int getStudentCountOfHighPowerNeeds() {
        return client.getStudentCountOfHighPowerNeeds();
    }

    @Override
    public ArrayList<Student> getStudentWithLowPowerNeeds() {
        return client.getStudentWithLowPowerNeeds();
    }

    @Override
    public int getStudentCountOfLowPowerNeeds() {
        return client.getStudentCountOfLowPowerNeeds();
    }

    @Override
    public Student createStudent(String name, Date degreeEndDate, String degreeTitle, int viaId, String email, int phoneNumber, PerformanceTypeEnum performanceNeeded) {
        return client.createStudent(name, degreeEndDate, degreeTitle, viaId, email, phoneNumber, performanceNeeded);
    }

    @Override
    public boolean deleteStudent(int viaId) {
        return client.deleteStudent(viaId);
    }

    // ReservationDataInterface methods

    @Override
    public Reservation createReservation(Student student, Laptop laptop) {
        return client.createReservation(student, laptop);
    }

    @Override
    public ArrayList<Student> getThoseWhoHaveLaptop() {
        return client.getThoseWhoHaveLaptop();
    }

    @Override
    public int getCountOfWhoHasLaptop() {
        return client.getCountOfWhoHasLaptop();
    }

    // ========== Additional Queue Methods ==========

    @Override
    public List<Student> getHighPerformanceQueue() {
        return client.getHighPerformanceQueue();
    }

    @Override
    public List<Student> getLowPerformanceQueue() {
        return client.getLowPerformanceQueue();
    }

    @Override
    public int getHighPerformanceQueueSize() {
        List<Student> queue = getHighPerformanceQueue();
        return queue != null ? queue.size() : 0;
    }

    @Override
    public int getLowPerformanceQueueSize() {
        List<Student> queue = getLowPerformanceQueue();
        return queue != null ? queue.size() : 0;
    }

    @Override
    public boolean addToHighPerformanceQueue(int studentId) {
        System.out.println("Client requesting to add student " + studentId + " to high performance queue");
        return client.addToHighPerformanceQueue(studentId);
    }

    @Override
    public boolean addToLowPerformanceQueue(int studentId) {
        System.out.println("Client requesting to add student " + studentId + " to low performance queue");
        return client.addToLowPerformanceQueue(studentId);
    }

    @Override
    public boolean removeFromHighPerformanceQueue(int studentId) {
        // Ikke direkte implementeret i socketClient - kræver tilføjelse til server API
        // Indtil videre vil vi returne false som standard
        return false;
    }

    @Override
    public boolean removeFromLowPerformanceQueue(int studentId) {
        // Ikke direkte implementeret i socketClient - kræver tilføjelse til server API
        // Indtil videre vil vi returne false som standard
        return false;
    }

    // ========== Additional Reservation Methods ==========

    @Override
    public List<Reservation> getAllReservations() {
        return client.getAllReservations();
    }

    @Override
    public List<Reservation> getActiveReservations() {
        return client.getActiveReservations();
    }

    @Override
    public List<Reservation> getReservationsByStudent(int studentId) {
        // Denne metode er ikke direkte tilgængelig via client
        // Vi laver en simpel implementering der filtrerer alle reservationer
        List<Reservation> allReservations = getAllReservations();
        List<Reservation> studentReservations = new ArrayList<>();

        if (allReservations != null) {
            for (Reservation reservation : allReservations) {
                if (reservation.getStudent().getViaId() == studentId) {
                    studentReservations.add(reservation);
                }
            }
        }

        return studentReservations;
    }

    @Override
    public List<Reservation> getReservationsByLaptop(UUID laptopId) {
        // Denne metode er ikke direkte tilgængelig via client
        // Vi laver en simpel implementering der filtrerer alle reservationer
        List<Reservation> allReservations = getAllReservations();
        List<Reservation> laptopReservations = new ArrayList<>();

        if (allReservations != null) {
            for (Reservation reservation : allReservations) {
                if (reservation.getLaptop().getId().equals(laptopId)) {
                    laptopReservations.add(reservation);
                }
            }
        }

        return laptopReservations;
    }

    @Override
    public boolean updateReservationStatus(UUID reservationId, ReservationStatusEnum newStatus) {
        // Denne metode er ikke direkte tilgængelig via client
        // Vi implementerer den afhængigt af status type
        if (newStatus == ReservationStatusEnum.COMPLETED) {
            return completeReservation(reservationId);
        } else if (newStatus == ReservationStatusEnum.CANCELLED) {
            return cancelReservation(reservationId);
        }

        return false;
    }

    @Override
    public boolean completeReservation(UUID reservationId) {
        return client.completeReservation(reservationId);
    }

    @Override
    public boolean cancelReservation(UUID reservationId) {
        // Denne metode er ikke direkte implementeret i client
        // Vil kræve tilføjelse af en metode i SocketClient og på serveren
        return false;
    }

    // ========== Process Queues Method ==========

    @Override
    public int processQueues() {
        return client.processQueues();
    }

    // PropertyChangeNotifier implementation

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