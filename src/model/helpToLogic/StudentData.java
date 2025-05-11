package model.helpToLogic;

import enums.PerformanceTypeEnum;
import objects.Laptop;
import objects.Student;
import util.PropertyChangeSubjectInterface;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class StudentData implements StudentDataInterface, PropertyChangeListener, PropertyChangeSubjectInterface {
    private List<Student> studentsCache;
    private PropertyChangeSupport support;

    public static final String EVENT_STUDENT_ADDED = "studentdata_student_added";
    public static final String EVENT_STUDENT_REMOVED = "studentdata_student_removed";
    public static final String EVENT_STUDENT_UPDATED = "studentdata_student_updated";
    public static final String EVENT_STUDENT_COUNT_CHANGED = "studentdata_count_changed";


    public StudentData(){
        studentsCache = new ArrayList<>();
        support = new PropertyChangeSupport(this);

        for (Student student : studentsCache){
            student.addListener(this);
        }
    }

    // Metoden fra StudentDataInterface

    @Override
    public ArrayList<Student> getAllStudents() {
        ArrayList<Student> allStudents = new ArrayList<>(studentsCache);
        return allStudents;
    }

    @Override
    public int getStudentCount() {
        return getAllStudents().size();
    }

    @Override
    public Student getStudentByID(int id) {
        for (Student student : studentsCache){
            if (student.getViaId() == id){
                return student;
            }
        }
        return null;
    }

    @Override
    public ArrayList<Student> getStudentWithHighPowerNeeds() {
        ArrayList<Student> highPowerStudents = new ArrayList<>();
        for (Student student : studentsCache){
            if (PerformanceTypeEnum.HIGH.equals(student.getPerformanceNeeded())){
                highPowerStudents.add(student);
            }
        }
        return highPowerStudents;
    }

    @Override
    public int getStudentCountOfHighPowerNeeds() {
        return getStudentWithHighPowerNeeds().size();
    }

    @Override
    public ArrayList<Student> getStudentWithLowPowerNeeds() {
        ArrayList<Student> lowPowerStudents = new ArrayList<>();
        for (Student student : studentsCache){
            if (PerformanceTypeEnum.LOW.equals(student.getPerformanceNeeded())){
                lowPowerStudents.add(student);
            }
        }
        return lowPowerStudents;
    }

    @Override
    public int getStudentCountOfLowPowerNeeds() {
        return getStudentWithLowPowerNeeds().size();
    }

    @Override
    public Student createStudent(String name, Date degreeEndDate, String degreeTitle, int viaId, String email, int phoneNumber, PerformanceTypeEnum performanceNeeded) {
        Student student = new Student(name, degreeEndDate, degreeTitle, viaId, email, phoneNumber, performanceNeeded);
        student.addListener(this);
        support.firePropertyChange(EVENT_STUDENT_ADDED, null, student);
        return student;
    }

    @Override
    public boolean deleteStudent(int viaId) {
        if (studentsCache.remove(getStudentByID(viaId))){
            support.firePropertyChange(EVENT_STUDENT_REMOVED, null, true);
            return true;
        } else return false;
    }

    // Metoden fra listener interface

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        if (Student.EVENT_NAME_CHANGED.equals(propertyName)){
            Student student = (Student) evt.getSource();

            support.firePropertyChange(EVENT_STUDENT_UPDATED, null, student);
        }
    }


    // Metoderne fra propertyChangeSubject (tilføjer lyttere)

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
