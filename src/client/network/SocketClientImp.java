package client.network;

import enums.PerformanceTypeEnum;
import model.helpToLogic.LaptopDataInterface;
import model.helpToLogic.ReservationsDataInterface;
import model.helpToLogic.StudentDataInterface;
import objects.Laptop;
import objects.Reservation;
import objects.Student;
import util.PropertyChangeSubjectInterface;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class SocketClientImp implements SocketClient, PropertyChangeSubjectInterface {
    private ObjectInputStream inFromServer;
    private ObjectOutputStream outFromServer;
    private SocketClient socket;
    private PropertyChangeSupport support;

    public SocketClientImp(String socketHost, int socketPort){
        support = new PropertyChangeSupport(this);
        try {
            System.out.println("Connecting to server");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    // Laptop data interface metoder

    @Override
    public List<Laptop> getAllLaptops() {
        return List.of();
    }

    @Override
    public List<Laptop> getAvailableLaptops() {
        return List.of();
    }

    @Override
    public List<Laptop> getLoanedLaptops() {
        return List.of();
    }

    @Override
    public Laptop seeNextAvailableLaptop(PerformanceTypeEnum performanceTypeEnum) {
        return null;
    }

    @Override
    public Laptop getLaptopByUUID(UUID id) {
        return null;
    }

    @Override
    public Laptop createLaptop(String brand, String model, int gigabyte, int ram, PerformanceTypeEnum performanceType) {
        return null;
    }

    @Override
    public Laptop updateLaptopState(UUID id) {
        return null;
    }

    @Override
    public Laptop deleteLaptop(UUID id) {
        return null;
    }

    // Reservation data interface

    @Override
    public Reservation createReservation(Student student, Laptop laptop) {
        return null;
    }

    @Override
    public ArrayList<Student> getThoseWhoHaveLaptop() {
        return null;
    }

    @Override
    public int getCountOfWhoHasLaptop() {
        return 0;
    }

    // Student data interface

    @Override
    public ArrayList<Student> getAllStudents() {
        return null;
    }

    @Override
    public int getStudentCount() {
        return 0;
    }

    @Override
    public Student getStudentByID(int id) {
        return null;
    }

    @Override
    public ArrayList<Student> getStudentWithHighPowerNeeds() {
        return null;
    }

    @Override
    public int getStudentCountOfHighPowerNeeds() {
        return 0;
    }

    @Override
    public ArrayList<Student> getStudentWithLowPowerNeeds() {
        return null;
    }

    @Override
    public int getStudentCountOfLowPowerNeeds() {
        return 0;
    }

    @Override
    public Student createStudent(String name, Date degreeEndDate, String degreeTitle, int viaId, String email, int phoneNumber, PerformanceTypeEnum performanceNeeded) {
        return null;
    }

    @Override
    public boolean deleteStudent(int viaId) {
        return false;
    }

    // Property change subject metoder (bruges med support)

    @Override
    public void addListener(PropertyChangeListener listener) {

    }

    @Override
    public void removeListener(PropertyChangeListener listener) {

    }

    @Override
    public void addListener(String propertyName, PropertyChangeListener listener) {

    }

    @Override
    public void removeListener(String propertyName, PropertyChangeListener listener) {

    }
}
