package model;

import model.helpToLogic.LaptopDataInterface;
import model.helpToLogic.ReservationsDataInterface;
import model.helpToLogic.StudentDataInterface;
import util.PropertyChangeSubjectInterface;

import java.beans.PropertyChangeListener;

public interface Model extends LaptopDataInterface, StudentDataInterface, ReservationsDataInterface, PropertyChangeListener, PropertyChangeSubjectInterface {

}
