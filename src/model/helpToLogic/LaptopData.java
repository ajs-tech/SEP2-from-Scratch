package model.helpToLogic;

import enums.PerformanceTypeEnum;
import objects.Laptop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LaptopData implements LaptopdataInterface, PropertyChangeListener {

    // Event types for observer notifications
    public static final String EVENT_LAPTOP_ADDED = "LAPTOP_ADDED";
    public static final String EVENT_LAPTOP_UPDATED = "LAPTOP_UPDATED";
    public static final String EVENT_LAPTOP_REMOVED = "LAPTOP_REMOVED";
    public static final String EVENT_LAPTOPS_REFRESHED = "LAPTOPS_REFRESHED";
    public static final String EVENT_ERROR = "ERROR";

    private List<Laptop> laptopChache;

    public LaptopData(){
        laptopChache = new ArrayList<>();
    }





    @Override
    public void propertyChange(PropertyChangeEvent evt) {

    }

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
    public Laptop updateLaptop(UUID id) {
        return null;
    }

    @Override
    public Laptop deleteLaptop(UUID id) {
        return null;
    }
}
