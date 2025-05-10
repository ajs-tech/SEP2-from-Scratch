package model.helpToLogic;

import enums.PerformanceTypeEnum;
import objects.Student;

import java.util.ArrayList;
import java.util.Date;

public interface StudentDataInterface {
    ArrayList<Student> getAllStudents();
    int getStudentCount();
    Student getStudentByID(int id);
    ArrayList<Student> getStudentWithHighPowerNeeds();
    int getStudentCountOfHighPowerNeeds();
    ArrayList<Student> getStudentWithLowPowerNeeds();
    int getStudentCountOfLowPowerNeeds();
    Student createStudent(String name, Date degreeEndDate, String degreeTitle,
                          int viaId, String email, int phoneNumber,
                          PerformanceTypeEnum performanceNeeded);

    boolean deleteStudent(int viaId);
}
