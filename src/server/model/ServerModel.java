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


    List<Student> getHighPerformanceQueue();

    List<Student> getLowPerformanceQueue();

    int getHighPerformanceQueueSize();

    int getLowPerformanceQueueSize();

    boolean addToHighPerformanceQueue(int studentId);

    boolean addToLowPerformanceQueue(int studentId);

    boolean removeFromHighPerformanceQueue(int studentId);

    boolean removeFromLowPerformanceQueue(int studentId);

    // ========== Additional Reservation Methods ==========

    List<Reservation> getAllReservations();

    List<Reservation> getActiveReservations();

    List<Reservation> getReservationsByStudent(int studentId);

    List<Reservation> getReservationsByLaptop(UUID laptopId);

    boolean updateReservationStatus(UUID reservationId, ReservationStatusEnum newStatus);

    boolean completeReservation(UUID reservationId);

    boolean cancelReservation(UUID reservationId);

    // ========== Utility Methods ==========

    boolean canAssignLaptop(PerformanceTypeEnum performanceType);

    int processQueues();
}
