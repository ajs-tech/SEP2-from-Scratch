package model;

import model.helpToLogic.LaptopData;
import model.helpToLogic.StudentData;
import objects.Student;

import java.util.ArrayList;

public class ModelImpl implements Model{

    private static ModelImpl INSTANCE;
    private LaptopData laptopData;
    private StudentData studentData;

    private ModelImpl(){
        laptopData = new LaptopData();
        studentData = new StudentData();
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








    @Override
    public ArrayList<Student> getThoseWhoHaveLaptop() {
        return null;
    }

    @Override
    public int getCountOfWhoHasLaptop() {
        return 0;
    }
}
