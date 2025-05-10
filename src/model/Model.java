package model;

import objects.Student;

import java.util.ArrayList;

public interface Model {
    ArrayList<Student> getThoseWhoHaveLaptop();
    int getCountOfWhoHasLaptop();
}
