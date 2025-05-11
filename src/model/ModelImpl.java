package model;

import client.network.SocketClient;
import core.ClientFactory;
import enums.PerformanceTypeEnum;
import model.helpToLogic.*;
import objects.Laptop;
import objects.Reservation;
import objects.Student;
import util.PropertyChangeSubjectInterface;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;


public class ModelImpl implements Model, LaptopDataInterface, StudentDataInterface, ReservationsDataInterface,PropertyChangeListener, PropertyChangeSubjectInterface {

    private static ModelImpl INSTANCE;
    private LaptopData laptopData;
    private StudentData studentData;
    private ReservationData reservationData;
    private SocketClient client;


    private ModelImpl(){
        laptopData = new LaptopData();
        studentData = new StudentData();
        reservationData = new ReservationData();
        laptopData.addListener(this);
        studentData.addListener(this);
        reservationData.addListener(this);
        client = ClientFactory.getClient();
    }

    public static ModelImpl getInstance(){
        if (INSTANCE == null){
            synchronized (ModelImpl.class){
                if (INSTANCE == null){
                    INSTANCE = new ModelImpl();
                    return INSTANCE;
                }
            }
        }
        return INSTANCE;
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


    // metoden fra listener interface
    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

    // metoderne fra subject interface (bruges med support!)


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
