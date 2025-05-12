package model.helpToLogic;

import enums.PerformanceTypeEnum;
import enums.ReservationStatusEnum;
import objects.Laptop;
import objects.Reservation;
import objects.Student;
import util.PropertyChangeSubjectInterface;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class ReservationData implements PropertyChangeSubjectInterface, PropertyChangeListener, ReservationsDataInterface {
    private List<Reservation> allReservations;
    private List<Reservation> activeReservations;
    private ArrayDeque<Student> highPerformanceQueue;
    private ArrayDeque<Student> lowPerformanceQueue;
    private PropertyChangeSupport support;

    public static String EVENT_STATUSCHANGEDCOMPLETED = "EVENT_STATUSCHANGEDCOMPLETED";

    public static final String EVENT_RESERVATION_ADDED = "resdata_added";
    public static final String EVENT_RESERVATION_REMOVED = "resdata_removed";
    public static final String EVENT_RESERVATION_UPDATED = "resdata_updated";
    public static final String EVENT_RESERVATION_COMPLETED = "resdata_completed";
    public static final String EVENT_RESERVATION_CANCELLED = "resdata_cancelled";
    public static final String EVENT_ACTIVE_COUNT_CHANGED = "resdata_active_count_changed";


    public ReservationData(){
        allReservations = new ArrayList<>();
        activeReservations = new ArrayList<>();
        highPerformanceQueue = new ArrayDeque<>();
        lowPerformanceQueue = new ArrayDeque<>();
        support = new PropertyChangeSupport(this);

        for (Reservation reservation : allReservations){
            reservation.addListener(this);
        }
    }

    // Metoder fra ReservationsDataInterface

    @Override
    public Reservation createReservation(Student student, Laptop laptop){
        Reservation reservation = new Reservation(student, laptop);
        return reservation;
    }

    @Override
    public ArrayList<Student> getThoseWhoHaveLaptop() {
        ArrayList<Student> thoseWhoHaveLaptop = new ArrayList<>();
        for (Reservation reservation : activeReservations){
            thoseWhoHaveLaptop.add(reservation.getStudent());
        }
        return getThoseWhoHaveLaptop();
    }

    @Override
    public int getCountOfWhoHasLaptop() {
        return getThoseWhoHaveLaptop().size();
    }


    // Metoden fra listener interface

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        if (evt.getSource() instanceof Reservation) {
            Reservation reservation = (Reservation) evt.getSource();

            // Handle reservation status changes
            if (Reservation.EVENT_STATUS_CHANGED.equals(propertyName)) {
                // Update internal structures
                if (reservation.getStatus() == ReservationStatusEnum.ACTIVE) {
                    if (!activeReservations.contains(reservation)) {
                        activeReservations.add(reservation);
                        support.firePropertyChange(EVENT_RESERVATION_ADDED, null, reservation);
                        support.firePropertyChange(EVENT_ACTIVE_COUNT_CHANGED,
                                activeReservations.size() - 1,
                                activeReservations.size());
                    }
                }
                else {
                    // Reservation completed or cancelled
                    if (activeReservations.remove(reservation)) {
                        support.firePropertyChange(EVENT_RESERVATION_REMOVED, reservation, null);
                        support.firePropertyChange(EVENT_ACTIVE_COUNT_CHANGED,
                                activeReservations.size() + 1,
                                activeReservations.size());

                        // Notify of specific completion type
                        if (reservation.getStatus() == ReservationStatusEnum.COMPLETED) {
                            support.firePropertyChange(EVENT_RESERVATION_COMPLETED, null, reservation);
                        } else if (reservation.getStatus() == ReservationStatusEnum.CANCELLED) {
                            support.firePropertyChange(EVENT_RESERVATION_CANCELLED, null, reservation);
                        }
                    }
                }
            }
        }
    }


    // Observer metoder (tilføjet lyttere)

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
