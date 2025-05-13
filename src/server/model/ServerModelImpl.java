package server.model;

import database.LaptopDAO;
import database.QueueDAO;
import database.ReservationDAO;
import database.StudentDAO;
import enums.PerformanceTypeEnum;
import enums.ReservationStatusEnum;
import objects.Laptop;
import objects.Reservation;
import objects.Student;
import util.Message;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the ServerModel interface.
 * Manages data access and provides a facade to the underlying DAOs.
 * Implements the Observer pattern to notify clients of changes.
 */
public class ServerModelImpl implements ServerModel {
    private static final Logger logger = Logger.getLogger(ServerModelImpl.class.getName());

    // DAOs for data access
    private final LaptopDAO laptopDAO;
    private final StudentDAO studentDAO;
    private final ReservationDAO reservationDAO;
    private final QueueDAO queueDAO;

    // Property change support for observer pattern
    private final PropertyChangeSupport support;

    // Event types for notifications
    public static final String EVENT_LAPTOP_CREATED = "server_laptop_created";
    public static final String EVENT_LAPTOP_UPDATED = "server_laptop_updated";
    public static final String EVENT_LAPTOP_DELETED = "server_laptop_deleted";
    public static final String EVENT_LAPTOP_STATE_CHANGED = "server_laptop_state_changed";

    public static final String EVENT_STUDENT_CREATED = "server_student_created";
    public static final String EVENT_STUDENT_UPDATED = "server_student_updated";
    public static final String EVENT_STUDENT_DELETED = "server_student_deleted";

    public static final String EVENT_RESERVATION_CREATED = "server_reservation_created";
    public static final String EVENT_RESERVATION_UPDATED = "server_reservation_updated";
    public static final String EVENT_RESERVATION_COMPLETED = "server_reservation_completed";
    public static final String EVENT_RESERVATION_CANCELLED = "server_reservation_cancelled";

    public static final String EVENT_QUEUE_STUDENT_ADDED = "server_queue_student_added";
    public static final String EVENT_QUEUE_STUDENT_REMOVED = "server_queue_student_removed";
    public static final String EVENT_QUEUE_UPDATED = "server_queue_updated";

    public static final String EVENT_ERROR = "server_error";

    /**
     * Creates a new ServerModelImpl instance.
     * Initializes the DAOs and sets up event listeners.
     */
    public ServerModelImpl() {
        this.laptopDAO = new LaptopDAO();
        this.studentDAO = new StudentDAO();
        this.reservationDAO = new ReservationDAO();
        this.queueDAO = new QueueDAO();
        this.support = new PropertyChangeSupport(this);

        // Set up listeners for DAO events
        setupDAOListeners();

        logger.info("ServerModelImpl initialized");
    }

    /**
     * Sets up listeners for all DAO events to propagate them to clients.
     */
    private void setupDAOListeners() {
        // LaptopDAO listeners
        laptopDAO.addListener(LaptopDAO.LAPTOP_CREATED, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_LAPTOP_CREATED, null, msg);
        });

        laptopDAO.addListener(LaptopDAO.LAPTOP_UPDATED, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_LAPTOP_UPDATED, null, msg);
        });

        laptopDAO.addListener(LaptopDAO.LAPTOP_DELETED, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_LAPTOP_DELETED, null, msg);
        });

        laptopDAO.addListener(LaptopDAO.LAPTOP_STATE_CHANGED, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_LAPTOP_STATE_CHANGED, null, msg);
        });

        // StudentDAO listeners
        studentDAO.addListener(StudentDAO.STUDENT_CREATED, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_STUDENT_CREATED, null, msg);
        });

        studentDAO.addListener(StudentDAO.STUDENT_UPDATED, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_STUDENT_UPDATED, null, msg);
        });

        studentDAO.addListener(StudentDAO.STUDENT_DELETED, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_STUDENT_DELETED, null, msg);
        });

        // ReservationDAO listeners
        reservationDAO.addListener(ReservationDAO.RESERVATION_CREATED, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_RESERVATION_CREATED, null, msg);
        });

        reservationDAO.addListener(ReservationDAO.RESERVATION_UPDATED, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_RESERVATION_UPDATED, null, msg);
        });

        reservationDAO.addListener(ReservationDAO.RESERVATION_STATUS_CHANGED, evt -> {
            Message msg = (Message) evt.getNewValue();
            Object[] args = (Object[]) msg.getArgs();
            Reservation reservation = (Reservation) args[0];
            ReservationStatusEnum oldStatus = (ReservationStatusEnum) args[1];
            ReservationStatusEnum newStatus = (ReservationStatusEnum) args[2];

            if (newStatus == ReservationStatusEnum.COMPLETED) {
                support.firePropertyChange(EVENT_RESERVATION_COMPLETED, null, reservation);
            } else if (newStatus == ReservationStatusEnum.CANCELLED) {
                support.firePropertyChange(EVENT_RESERVATION_CANCELLED, null, reservation);
            }
        });

        // QueueDAO listeners
        queueDAO.addListener(QueueDAO.QUEUE_UPDATED, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_QUEUE_UPDATED, null, msg);
        });

        // Error listeners from all DAOs
        laptopDAO.addListener(LaptopDAO.DATABASE_ERROR, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_ERROR, null, msg);
        });

        studentDAO.addListener(StudentDAO.DATABASE_ERROR, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_ERROR, null, msg);
        });

        reservationDAO.addListener(ReservationDAO.DATABASE_ERROR, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_ERROR, null, msg);
        });

        queueDAO.addListener(QueueDAO.DATABASE_ERROR, evt -> {
            Message msg = (Message) evt.getNewValue();
            support.firePropertyChange(EVENT_ERROR, null, msg);
        });
    }

    /**
     * Handles errors by logging them and firing a property change event.
     *
     * @param message Error message
     * @param e The exception that occurred
     */
    private void handleError(String message, Exception e) {
        logger.log(Level.SEVERE, message, e);
        Message errorMsg = new Message(EVENT_ERROR, message + ": " + e.getMessage());
        support.firePropertyChange(EVENT_ERROR, null, errorMsg);
    }

    // ========== PropertyChangeListener Methods ==========

    /**
     * Adds a listener for all property changes.
     *
     * @param listener The listener to add
     */
    @Override
    public void addListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    /**
     * Removes a listener for all property changes.
     *
     * @param listener The listener to remove
     */
    @Override
    public void removeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    /**
     * Adds a listener for specific property changes.
     *
     * @param propertyName The event name to listen for
     * @param listener The listener to add
     */
    @Override
    public void addListener(String propertyName, PropertyChangeListener listener) {
        support.addPropertyChangeListener(propertyName, listener);
    }

    /**
     * Removes a listener for specific property changes.
     *
     * @param propertyName The event name to stop listening for
     * @param listener The listener to remove
     */
    @Override
    public void removeListener(String propertyName, PropertyChangeListener listener) {
        support.removePropertyChangeListener(propertyName, listener);
    }

    // ========== Laptop Methods ==========

    /**
     * Gets all laptops from the database.
     *
     * @return List of all laptops
     */
    @Override
    public List<Laptop> getAllLaptops() {
        try {
            return laptopDAO.getAll();
        } catch (SQLException e) {
            handleError("Error getting all laptops", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets all available laptops.
     *
     * @return List of available laptops
     */
    @Override
    public List<Laptop> getAvailableLaptops() {
        try {
            List<Laptop> allLaptops = laptopDAO.getAll();
            List<Laptop> availableLaptops = new ArrayList<>();

            for (Laptop laptop : allLaptops) {
                if (laptop.isAvailable()) {
                    availableLaptops.add(laptop);
                }
            }

            return availableLaptops;
        } catch (SQLException e) {
            handleError("Error getting available laptops", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets all loaned laptops.
     *
     * @return List of loaned laptops
     */
    @Override
    public List<Laptop> getLoanedLaptops() {
        try {
            List<Laptop> allLaptops = laptopDAO.getAll();
            List<Laptop> loanedLaptops = new ArrayList<>();

            for (Laptop laptop : allLaptops) {
                if (laptop.isLoaned()) {
                    loanedLaptops.add(laptop);
                }
            }

            return loanedLaptops;
        } catch (SQLException e) {
            handleError("Error getting loaned laptops", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets the next available laptop with the specified performance type.
     *
     * @param performanceTypeEnum The required performance type
     * @return The next available laptop or null if none found
     */
    @Override
    public Laptop seeNextAvailableLaptop(PerformanceTypeEnum performanceTypeEnum) {
        try {
            List<Laptop> allLaptops = laptopDAO.getAll();

            for (Laptop laptop : allLaptops) {
                if (laptop.isAvailable() && laptop.getPerformanceType() == performanceTypeEnum) {
                    return laptop;
                }
            }

            return null;
        } catch (SQLException e) {
            handleError("Error getting next available laptop", e);
            return null;
        }
    }

    /**
     * Gets a laptop by its UUID.
     *
     * @param id The laptop UUID
     * @return The laptop or null if not found
     */
    @Override
    public Laptop getLaptopByUUID(UUID id) {
        try {
            return laptopDAO.getById(id);
        } catch (SQLException e) {
            handleError("Error getting laptop by UUID: " + id, e);
            return null;
        }
    }

    /**
     * Creates a new laptop.
     *
     * @param brand Laptop brand
     * @param model Laptop model
     * @param gigabyte Storage capacity in GB
     * @param ram RAM capacity in GB
     * @param performanceType Laptop performance type
     * @return The created laptop or null if creation failed
     */
    @Override
    public Laptop createLaptop(String brand, String model, int gigabyte, int ram, PerformanceTypeEnum performanceType) {
        try {
            Laptop laptop = new Laptop(brand, model, gigabyte, ram, performanceType);
            boolean success = laptopDAO.insert(laptop);
            return success ? laptop : null;
        } catch (SQLException e) {
            handleError("Error creating laptop", e);
            return null;
        }
    }

    /**
     * Updates a laptop's state.
     *
     * @param id The laptop UUID
     * @return The updated laptop or null if update failed
     */
    @Override
    public Laptop updateLaptopState(UUID id) {
        try {
            Laptop laptop = laptopDAO.getById(id);
            if (laptop != null) {
                laptop.setState(); // This triggers the state change
                boolean success = laptopDAO.updateState(laptop);
                return success ? laptop : null;
            }
            return null;
        } catch (SQLException e) {
            handleError("Error updating laptop state: " + id, e);
            return null;
        }
    }

    /**
     * Deletes a laptop by its UUID.
     *
     * @param id The laptop UUID
     * @return The deleted laptop or null if deletion failed
     */
    @Override
    public Laptop deleteLaptop(UUID id) {
        try {
            Laptop laptop = laptopDAO.getById(id);
            if (laptop != null && laptopDAO.delete(id)) {
                return laptop;
            }
            return null;
        } catch (SQLException e) {
            handleError("Error deleting laptop: " + id, e);
            return null;
        }
    }

    // ========== Student Methods ==========

    /**
     * Gets all students from the database.
     *
     * @return List of all students
     */
    @Override
    public ArrayList<Student> getAllStudents() {
        try {
            return new ArrayList<>(studentDAO.getAll());
        } catch (SQLException e) {
            handleError("Error getting all students", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets the total number of students.
     *
     * @return Count of students
     */
    @Override
    public int getStudentCount() {
        try {
            return studentDAO.count();
        } catch (SQLException e) {
            handleError("Error getting student count", e);
            return 0;
        }
    }

    /**
     * Gets a student by their VIA ID.
     *
     * @param id The student's VIA ID
     * @return The student or null if not found
     */
    @Override
    public Student getStudentByID(int id) {
        try {
            return studentDAO.getById(id);
        } catch (SQLException e) {
            handleError("Error getting student by ID: " + id, e);
            return null;
        }
    }

    /**
     * Gets all students with high power needs.
     *
     * @return List of students with high power needs
     */
    @Override
    public ArrayList<Student> getStudentWithHighPowerNeeds() {
        try {
            return new ArrayList<>(studentDAO.getByPerformanceType(PerformanceTypeEnum.HIGH));
        } catch (SQLException e) {
            handleError("Error getting students with high power needs", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets the count of students with high power needs.
     *
     * @return Count of students with high power needs
     */
    @Override
    public int getStudentCountOfHighPowerNeeds() {
        try {
            return studentDAO.getByPerformanceType(PerformanceTypeEnum.HIGH).size();
        } catch (SQLException e) {
            handleError("Error getting count of students with high power needs", e);
            return 0;
        }
    }

    /**
     * Gets all students with low power needs.
     *
     * @return List of students with low power needs
     */
    @Override
    public ArrayList<Student> getStudentWithLowPowerNeeds() {
        try {
            return new ArrayList<>(studentDAO.getByPerformanceType(PerformanceTypeEnum.LOW));
        } catch (SQLException e) {
            handleError("Error getting students with low power needs", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets the count of students with low power needs.
     *
     * @return Count of students with low power needs
     */
    @Override
    public int getStudentCountOfLowPowerNeeds() {
        try {
            return studentDAO.getByPerformanceType(PerformanceTypeEnum.LOW).size();
        } catch (SQLException e) {
            handleError("Error getting count of students with low power needs", e);
            return 0;
        }
    }

    /**
     * Creates a new student.
     *
     * @param name The student's name
     * @param degreeEndDate The end date of the student's degree
     * @param degreeTitle The title of the student's degree
     * @param viaId The student's VIA ID
     * @param email The student's email
     * @param phoneNumber The student's phone number
     * @param performanceNeeded The student's laptop performance needs
     * @return The created student or null if creation failed
     */
    @Override
    public Student createStudent(String name, Date degreeEndDate, String degreeTitle,
                                 int viaId, String email, int phoneNumber, PerformanceTypeEnum performanceNeeded) {
        try {
            // Create the student first
            Student student = new Student(name, degreeEndDate, degreeTitle, viaId,
                    email, phoneNumber, performanceNeeded);
            boolean success = studentDAO.insert(student);

            if (!success) {
                return null;
            }

            // NOW AUTOMATICALLY TRY TO ASSIGN A LAPTOP!
            Laptop availableLaptop = seeNextAvailableLaptop(performanceNeeded);

            if (availableLaptop != null) {
                // Create reservation automatically
                Reservation reservation = createReservation(student, availableLaptop);

                if (reservation != null) {
                    logger.info("Automatically assigned laptop " + availableLaptop.getId() +
                            " to student " + student.getViaId());
                } else {
                    // Failed to create reservation, add to queue
                    addStudentToQueue(student, performanceNeeded);
                }
            } else {
                // No laptop available, add to queue
                addStudentToQueue(student, performanceNeeded);
            }

            return student;
        } catch (SQLException e) {
            handleError("Error creating student", e);
            return null;
        }
    }

    private void addStudentToQueue(Student student, PerformanceTypeEnum performanceNeeded) {
        try {
            if (performanceNeeded == PerformanceTypeEnum.HIGH) {
                addToHighPerformanceQueue(student.getViaId());
                logger.info("Added student " + student.getViaId() + " to high performance queue");
            } else {
                addToLowPerformanceQueue(student.getViaId());
                logger.info("Added student " + student.getViaId() + " to low performance queue");
            }
        } catch (Exception e) {
            logger.warning("Failed to add student to queue: " + e.getMessage());
        }
    }

    /**
     * Deletes a student by their VIA ID.
     *
     * @param viaId The student's VIA ID
     * @return True if deletion succeeded, false otherwise
     */
    @Override
    public boolean deleteStudent(int viaId) {
        try {
            return studentDAO.delete(viaId);
        } catch (SQLException e) {
            handleError("Error deleting student: " + viaId, e);
            return false;
        }
    }

    // ========== Reservation Methods ==========

    /**
     * Creates a new reservation.
     *
     * @param student The student borrowing the laptop
     * @param laptop The laptop being borrowed
     * @return The created reservation or null if creation failed
     */
    @Override
    public Reservation createReservation(Student student, Laptop laptop) {
        try {
            Reservation reservation = new Reservation(student, laptop);
            boolean success = reservationDAO.createReservationWithTransaction(reservation);
            return success ? reservation : null;
        } catch (SQLException e) {
            handleError("Error creating reservation", e);
            return null;
        }
    }

    /**
     * Gets all students who have a laptop.
     *
     * @return List of students with active reservations
     */
    @Override
    public ArrayList<Student> getThoseWhoHaveLaptop() {
        try {
            List<Student> studentsWithLaptops = reservationDAO.getStudentsWithActiveLaptop();
            return new ArrayList<>(studentsWithLaptops);
        } catch (SQLException e) {
            handleError("Error getting students with laptops", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets the count of students who have a laptop.
     *
     * @return Count of students with active reservations
     */
    @Override
    public int getCountOfWhoHasLaptop() {
        try {
            return reservationDAO.countActive();
        } catch (SQLException e) {
            handleError("Error getting count of students with laptops", e);
            return 0;
        }
    }

    /**
     * Gets all reservations from the database.
     *
     * @return List of all reservations
     */
    public List<Reservation> getAllReservations() {
        try {
            return reservationDAO.getAll();
        } catch (SQLException e) {
            handleError("Error getting all reservations", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets all active reservations.
     *
     * @return List of active reservations
     */
    public List<Reservation> getActiveReservations() {
        try {
            return reservationDAO.getAllActive();
        } catch (SQLException e) {
            handleError("Error getting active reservations", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets all reservations for a specific student.
     *
     * @param studentId The student's VIA ID
     * @return List of reservations for the student
     */
    public List<Reservation> getReservationsByStudent(int studentId) {
        try {
            return reservationDAO.getByStudentId(studentId);
        } catch (SQLException e) {
            handleError("Error getting reservations for student: " + studentId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets all reservations for a specific laptop.
     *
     * @param laptopId The laptop's UUID
     * @return List of reservations for the laptop
     */
    public List<Reservation> getReservationsByLaptop(UUID laptopId) {
        try {
            return reservationDAO.getByLaptopId(laptopId);
        } catch (SQLException e) {
            handleError("Error getting reservations for laptop: " + laptopId, e);
            return new ArrayList<>();
        }
    }

    /**
     * Updates a reservation's status.
     *
     * @param reservationId The reservation UUID
     * @param newStatus The new status
     * @return True if update succeeded, false otherwise
     */
    public boolean updateReservationStatus(UUID reservationId, ReservationStatusEnum newStatus) {
        try {
            Reservation reservation = reservationDAO.getById(reservationId);
            if (reservation != null) {
                reservation.changeStatus(newStatus);
                return reservationDAO.updateStatusWithTransaction(reservation);
            }
            return false;
        } catch (SQLException e) {
            handleError("Error updating reservation status: " + reservationId, e);
            return false;
        }
    }

    /**
     * Completes a reservation (returns a laptop).
     *
     * @param reservationId The reservation UUID
     * @return True if completion succeeded, false otherwise
     */
    public boolean completeReservation(UUID reservationId) {
        return updateReservationStatus(reservationId, ReservationStatusEnum.COMPLETED);
    }

    /**
     * Cancels a reservation.
     *
     * @param reservationId The reservation UUID
     * @return True if cancellation succeeded, false otherwise
     */
    public boolean cancelReservation(UUID reservationId) {
        return updateReservationStatus(reservationId, ReservationStatusEnum.CANCELLED);
    }

    // ========== Queue Methods ==========

    /**
     * Gets all students in the high performance queue.
     *
     * @return List of students in the high performance queue
     */
    public List<Student> getHighPerformanceQueue() {
        try {
            return queueDAO.getQueueByPerformanceType(PerformanceTypeEnum.HIGH);
        } catch (SQLException e) {
            handleError("Error getting high performance queue", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets all students in the low performance queue.
     *
     * @return List of students in the low performance queue
     */
    public List<Student> getLowPerformanceQueue() {
        try {
            return queueDAO.getQueueByPerformanceType(PerformanceTypeEnum.LOW);
        } catch (SQLException e) {
            handleError("Error getting low performance queue", e);
            return new ArrayList<>();
        }
    }

    /**
     * Gets the size of the high performance queue.
     *
     * @return Size of the high performance queue
     */
    public int getHighPerformanceQueueSize() {
        try {
            return queueDAO.getQueueSize(PerformanceTypeEnum.HIGH);
        } catch (SQLException e) {
            handleError("Error getting high performance queue size", e);
            return 0;
        }
    }

    /**
     * Gets the size of the low performance queue.
     *
     * @return Size of the low performance queue
     */
    public int getLowPerformanceQueueSize() {
        try {
            return queueDAO.getQueueSize(PerformanceTypeEnum.LOW);
        } catch (SQLException e) {
            handleError("Error getting low performance queue size", e);
            return 0;
        }
    }

    public boolean addToHighPerformanceQueue(int studentId) {
        try {
            System.out.println("Adding student " + studentId + " to high performance queue");
            boolean result = queueDAO.addToQueue(studentId, PerformanceTypeEnum.HIGH);
            System.out.println("Result: " + result);
            return result;
        } catch (SQLException e) {
            System.err.println("Error adding student to high performance queue: " + e.getMessage());
            e.printStackTrace();
            handleError("Error adding student to high performance queue: " + studentId, e);
            return false;
        }
    }

    public boolean addToLowPerformanceQueue(int studentId) {
        try {
            System.out.println("Adding student " + studentId + " to low performance queue");
            boolean result = queueDAO.addToQueue(studentId, PerformanceTypeEnum.LOW);
            System.out.println("Result: " + result);
            return result;
        } catch (SQLException e) {
            System.err.println("Error adding student to low performance queue: " + e.getMessage());
            e.printStackTrace();
            handleError("Error adding student to low performance queue: " + studentId, e);
            return false;
        }
    }

    /**
     * Removes a student from the high performance queue.
     *
     * @param studentId The student's VIA ID
     * @return True if removal succeeded, false otherwise
     */
    public boolean removeFromHighPerformanceQueue(int studentId) {
        try {
            return queueDAO.removeFromQueue(studentId, PerformanceTypeEnum.HIGH);
        } catch (SQLException e) {
            handleError("Error removing student from high performance queue: " + studentId, e);
            return false;
        }
    }

    /**
     * Removes a student from the low performance queue.
     *
     * @param studentId The student's VIA ID
     * @return True if removal succeeded, false otherwise
     */
    public boolean removeFromLowPerformanceQueue(int studentId) {
        try {
            return queueDAO.removeFromQueue(studentId, PerformanceTypeEnum.LOW);
        } catch (SQLException e) {
            handleError("Error removing student from low performance queue: " + studentId, e);
            return false;
        }
    }

    /**
     * Checks if a student can be assigned a laptop of the specified performance type.
     *
     * @param performanceType The performance type to check
     * @return True if a laptop is available, false otherwise
     */
    public boolean canAssignLaptop(PerformanceTypeEnum performanceType) {
        return seeNextAvailableLaptop(performanceType) != null;
    }

    /**
     * Processes the queues and assigns laptops where possible.
     *
     * @return The number of laptops that were assigned
     */
    @Override
    public int processQueues() {
        int assigned = 0;

        try {
            // First process high performance queue
            List<Student> highQueue = getHighPerformanceQueue();

            for (Student student : highQueue) {
                Laptop laptop = seeNextAvailableLaptop(PerformanceTypeEnum.HIGH);
                if (laptop == null) {
                    break; // No more high performance laptops available
                }

                Reservation reservation = createReservation(student, laptop);
                if (reservation != null) {
                    removeFromHighPerformanceQueue(student.getViaId());
                    assigned++;
                    logger.info("Assigned laptop " + laptop.getId() + " to student " +
                            student.getViaId() + " from high performance queue");
                }
            }

            // Then process low performance queue
            List<Student> lowQueue = getLowPerformanceQueue();

            for (Student student : lowQueue) {
                Laptop laptop = seeNextAvailableLaptop(PerformanceTypeEnum.LOW);
                if (laptop == null) {
                    break; // No more low performance laptops available
                }

                Reservation reservation = createReservation(student, laptop);
                if (reservation != null) {
                    removeFromLowPerformanceQueue(student.getViaId());
                    assigned++;
                    logger.info("Assigned laptop " + laptop.getId() + " to student " +
                            student.getViaId() + " from low performance queue");
                }
            }

            return assigned;
        } catch (Exception e) {
            handleError("Error processing queues", e);
            return assigned;
        }
    }


}
