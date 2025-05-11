package model.helpToLogic;

import enums.PerformanceTypeEnum;
import objects.AvailableState;
import objects.Laptop;
import objects.LaptopState;
import objects.LoanedState;
import util.PropertyChangeSubjectInterface;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LaptopData implements LaptopDataInterface, PropertyChangeListener, PropertyChangeSubjectInterface {

    // Event types for observer notifications
    public static final String EVENT_LAPTOP_ADDED = "LAPTOP_ADDED";
    public static final String EVENT_LAPTOPSTATE_UPDATED = "LAPTOPSTATE_UPDATED";
    public static final String EVENT_LAPTOP_REMOVED = "LAPTOP_REMOVED";
    public static final String EVENT_LAPTOPS_REFRESHED = "LAPTOPS_REFRESHED";
    public static final String EVENT_ERROR = "ERROR";

    private List<Laptop> laptopCache;
    private PropertyChangeSupport support;


    public LaptopData(){
        laptopCache = new ArrayList<>();
        support = new PropertyChangeSupport(this);

        for (Laptop laptop : laptopCache){
            laptop.addListener(this);
        }
    }


    // Metoder fra LaptopDataInterface (MED LOGIKKEN)

    @Override
    public List<Laptop> getAllLaptops() {
        return laptopCache;
    }

    @Override
    public List<Laptop> getAvailableLaptops() {
        ArrayList<Laptop> availableLaptops = new ArrayList<>();
        for (Laptop laptop : laptopCache){
            if (laptop.isAvailable()){
                availableLaptops.add(laptop);
            }
        }
        return availableLaptops;
    }

    @Override
    public List<Laptop> getLoanedLaptops() {
        ArrayList<Laptop> loanedLaptops = new ArrayList<>();
        for (Laptop laptop : laptopCache){
            if (laptop.isAvailable()){
                loanedLaptops.add(laptop);
            }
        }
        return loanedLaptops;
    }

    @Override
    public Laptop seeNextAvailableLaptop(PerformanceTypeEnum performanceTypeEnum) {
        for (Laptop laptop : laptopCache){
            if (laptop.isAvailable()){
                return laptop;
            }
        }
        return null;
    }

    @Override
    public Laptop getLaptopByUUID(UUID id) {
        for (Laptop laptop : laptopCache){
            if (laptop.getId().equals(id)){
                return laptop;
            }
        }
        return null;
    }

    @Override
    public Laptop createLaptop(String brand, String model, int gigabyte, int ram, PerformanceTypeEnum performanceType) {
        Laptop laptop = new Laptop(brand, model, gigabyte, ram, performanceType);
        laptopCache.add(laptop);
        laptop.addListener(this);
        return laptop;
    }

    @Override
    public Laptop updateLaptopState(UUID id) {
        Laptop laptop = getLaptopByUUID(id);
        laptop.setState();
        return laptop;
    }

    @Override
    public Laptop deleteLaptop(UUID id) {
        Laptop laptop = getLaptopByUUID(id);
        laptopCache.remove(laptop);
        return laptop;
    }

    // Metoder til support (DEM DER TILFØJER LYTTERE OG SENDER UD)

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


    // Metoden til listener (DEN DER LYTTER EFTER)

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        Object source = evt.getSource();
        String propertyName = evt.getPropertyName();

        if (source instanceof Laptop){
            Laptop laptop = (Laptop) source;

            if (EVENT_LAPTOPSTATE_UPDATED.equals(propertyName)){
                String oldValue = (String) evt.getOldValue();
                String newValue = (String) evt.getNewValue();

                if (AvailableState.simpleName.equals(newValue)){
                    // Søg efter student og tildel computer
                    // Opdater UI
                } else if (LoanedState.simpleName.equals(newValue)) {
                    // Opdater UI
                }

            }
        }
    }
}
