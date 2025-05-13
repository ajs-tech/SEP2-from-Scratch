package model;

import client.network.SocketClient;
import enums.ReservationStatusEnum;
import model.helpToLogic.LaptopDataInterface;
import model.helpToLogic.ReservationsDataInterface;
import model.helpToLogic.StudentDataInterface;
import objects.Reservation;
import objects.Student;
import util.PropertyChangeSubjectInterface;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.UUID;

public interface Model extends LaptopDataInterface, StudentDataInterface, ReservationsDataInterface, PropertyChangeListener, PropertyChangeSubjectInterface {
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

    // ========== Process Queues Method ==========
    int processQueues();

    // ========== Client Access ==========
    SocketClient getClient();
}
