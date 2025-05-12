package server.model;

import model.helpToLogic.LaptopDataInterface;
import model.helpToLogic.ReservationsDataInterface;
import model.helpToLogic.StudentDataInterface;
import enums.PerformanceTypeEnum;
import enums.ReservationStatusEnum;
import objects.Laptop;
import objects.Reservation;
import objects.Student;
import util.PropertyChangeSubjectInterface;

import java.util.List;
import java.util.UUID;

/**
 * Server-side model interface for the laptop management system.
 * Extends existing data interfaces for consistent method signatures.
 * Provides methods for managing laptops, students, reservations, and queues.
 */
public interface ServerModel extends LaptopDataInterface, StudentDataInterface,
        ReservationsDataInterface, PropertyChangeSubjectInterface {

    // ========== Additional Queue Methods ==========

    /**
     * Gets all students in the high performance queue.
     *
     * @return List of students in the high performance queue
     */
    List<Student> getHighPerformanceQueue();

    /**
     * Gets all students in the low performance queue.
     *
     * @return List of students in the low performance queue
     */
    List<Student> getLowPerformanceQueue();

    /**
     * Gets the size of the high performance queue.
     *
     * @return Size of the high performance queue
     */
    int getHighPerformanceQueueSize();

    /**
     * Gets the size of the low performance queue.
     *
     * @return Size of the low performance queue
     */
    int getLowPerformanceQueueSize();

    /**
     * Adds a student to the high performance queue.
     *
     * @param studentId The student's VIA ID
     * @return True if addition succeeded, false otherwise
     */
    boolean addToHighPerformanceQueue(int studentId);

    /**
     * Adds a student to the low performance queue.
     *
     * @param studentId The student's VIA ID
     * @return True if addition succeeded, false otherwise
     */
    boolean addToLowPerformanceQueue(int studentId);

    /**
     * Removes a student from the high performance queue.
     *
     * @param studentId The student's VIA ID
     * @return True if removal succeeded, false otherwise
     */
    boolean removeFromHighPerformanceQueue(int studentId);

    /**
     * Removes a student from the low performance queue.
     *
     * @param studentId The student's VIA ID
     * @return True if removal succeeded, false otherwise
     */
    boolean removeFromLowPerformanceQueue(int studentId);

    // ========== Additional Reservation Methods ==========

    /**
     * Gets all reservations from the database.
     *
     * @return List of all reservations
     */
    List<Reservation> getAllReservations();

    /**
     * Gets all active reservations.
     *
     * @return List of active reservations
     */
    List<Reservation> getActiveReservations();

    /**
     * Gets all reservations for a specific student.
     *
     * @param studentId The student's VIA ID
     * @return List of reservations for the student
     */
    List<Reservation> getReservationsByStudent(int studentId);

    /**
     * Gets all reservations for a specific laptop.
     *
     * @param laptopId The laptop's UUID
     * @return List of reservations for the laptop
     */
    List<Reservation> getReservationsByLaptop(UUID laptopId);

    /**
     * Updates a reservation's status.
     *
     * @param reservationId The reservation UUID
     * @param newStatus The new status
     * @return True if update succeeded, false otherwise
     */
    boolean updateReservationStatus(UUID reservationId, ReservationStatusEnum newStatus);

    /**
     * Completes a reservation (returns a laptop).
     *
     * @param reservationId The reservation UUID
     * @return True if completion succeeded, false otherwise
     */
    boolean completeReservation(UUID reservationId);

    /**
     * Cancels a reservation.
     *
     * @param reservationId The reservation UUID
     * @return True if cancellation succeeded, false otherwise
     */
    boolean cancelReservation(UUID reservationId);

    // ========== Utility Methods ==========

    /**
     * Checks if a student can be assigned a laptop of the specified performance type.
     *
     * @param performanceType The performance type to check
     * @return True if a laptop is available, false otherwise
     */
    boolean canAssignLaptop(PerformanceTypeEnum performanceType);

    /**
     * Processes the queues and assigns laptops where possible.
     *
     * @return The number of laptops that were assigned
     */
    int processQueues();
}