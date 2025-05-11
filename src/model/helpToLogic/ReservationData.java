package model.helpToLogic;

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

        if (Reservation.EVENT_CHANGEDSTATUS.equals(evt.getPropertyName())){
            Reservation reservation = (Reservation) evt.getSource();
            if (evt.getNewValue() != ReservationStatusEnum.ACTIVE){
                activeReservations.remove(reservation);
                support.firePropertyChange(EVENT_STATUSCHANGEDCOMPLETED, false, true);
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
