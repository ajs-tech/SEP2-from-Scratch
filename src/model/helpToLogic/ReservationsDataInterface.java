package model.helpToLogic;

import objects.Laptop;
import objects.Reservation;
import objects.Student;

import java.util.ArrayList;

public interface ReservationsDataInterface {
    Reservation createReservation(Student student, Laptop laptop);
    ArrayList<Student> getThoseWhoHaveLaptop();
    int getCountOfWhoHasLaptop();
}
