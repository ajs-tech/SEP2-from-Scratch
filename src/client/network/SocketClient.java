package client.network;

import model.helpToLogic.LaptopDataInterface;
import model.helpToLogic.ReservationsDataInterface;
import model.helpToLogic.StudentDataInterface;
import objects.Reservation;
import objects.Student;
import util.Message;
import util.PropertyChangeSubjectInterface;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface SocketClient extends LaptopDataInterface, StudentDataInterface, ReservationsDataInterface, PropertyChangeSubjectInterface {


    // ===== Additional Methods =====

    public List<Reservation> getAllReservations();


    public List<Reservation> getActiveReservations();


    public boolean completeReservation(UUID reservationId);

    List<Student> getHighPerformanceQueue();

    List<Student> getLowPerformanceQueue();

    boolean addToHighPerformanceQueue(int studentId);

    boolean addToLowPerformanceQueue(int studentId);

    int processQueues();

    boolean isConnected();
}
