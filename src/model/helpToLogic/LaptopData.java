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

    public static final String EVENT_LAPTOP_ADDED = "laptopdata_laptop_added";
    public static final String EVENT_LAPTOP_REMOVED = "laptopdata_laptop_removed";
    public static final String EVENT_LAPTOP_UPDATED = "laptopdata_laptop_updated";
    public static final String EVENT_LAPTOP_STATE_CHANGED = "laptopdata_state_changed";
    public static final String EVENT_AVAILABLE_COUNT_CHANGED = "laptopdata_available_count_changed";
    public static final String EVENT_LOANED_COUNT_CHANGED = "laptopdata_loaned_count_changed";
    public static final String EVENT_LAPTOP_BECAME_AVAILABLE = "laptop_became_available";

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
        String propertyName = evt.getPropertyName();

        if (evt.getSource() instanceof Laptop) {
            Laptop laptop = (Laptop) evt.getSource();

            // Handle laptop state changes
            if (Laptop.EVENT_STATE_CHANGED.equals(propertyName)) {
                // First, notify about general state change
                support.firePropertyChange(EVENT_LAPTOP_STATE_CHANGED,
                        evt.getOldValue(),
                        evt.getNewValue());

                // Second, if availability changed, notify separately to update counts
                // and potentially check queues
                if (Laptop.EVENT_AVAILABILITY_CHANGED.equals(propertyName)) {
                    boolean isNowAvailable = (boolean) evt.getNewValue();

                    // Update available/loaned counts
                    int availableCount = getAvailableLaptops().size();
                    int loanedCount = getLoanedLaptops().size();

                    // Notify about updated counts (for UI updates)
                    support.firePropertyChange(EVENT_AVAILABLE_COUNT_CHANGED, null, availableCount);
                    support.firePropertyChange(EVENT_LOANED_COUNT_CHANGED, null, loanedCount);

                    // Specially notify if laptop became available (for queue processing)
                    if (isNowAvailable) {
                        support.firePropertyChange(EVENT_LAPTOP_BECAME_AVAILABLE, null, laptop);
                    }
                }
            }
        }
    }
}
