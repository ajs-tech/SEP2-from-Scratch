package objects;

import enums.PerformanceTypeEnum;
import model.helpToLogic.LaptopData;
import util.PropertyChangeSubjectInterface;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Objects;
import java.util.UUID;


public class Laptop implements PropertyChangeSubjectInterface {
    private UUID id;
    private String brand;
    private String model;
    private int gigabyte;
    private int ram;
    private PerformanceTypeEnum performanceType;
    private LaptopState state;
    private PropertyChangeSupport support;

    public static final String EVENT_BRAND_CHANGED = "laptop_brand_changed";
    public static final String EVENT_MODEL_CHANGED = "laptop_model_changed";
    public static final String EVENT_RAM_CHANGED = "laptop_ram_changed";
    public static final String EVENT_DISK_CHANGED = "laptop_disk_changed";
    public static final String EVENT_PERFORMANCE_CHANGED = "laptop_performance_changed";
    public static final String EVENT_STATE_CHANGED = "laptop_state_changed";
    public static final String EVENT_AVAILABILITY_CHANGED = "laptop_availability_changed";


    public Laptop(String brand, String model, int gigabyte, int ram, PerformanceTypeEnum performanceType) {
        this(UUID.randomUUID(), brand, model, gigabyte, ram, performanceType);
        support = new PropertyChangeSupport(this);
    }

    public Laptop(UUID id, String brand, String model, int gigabyte, int ram, PerformanceTypeEnum performanceType) {
        validateInput(brand, model, gigabyte, ram, performanceType);
        
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.gigabyte = gigabyte;
        this.ram = ram;
        this.performanceType = performanceType;
        this.state = new AvailableState();
    }
    
    /**
     * Validates all input parameters.
     * Throws exception with clear message about which validation failed.
     */
    private void validateInput(String brand, String model, int gigabyte, int ram, PerformanceTypeEnum performanceType) {
        if (brand == null || brand.trim().isEmpty()) {
            throw new IllegalArgumentException("Brand cannot be empty");
        }
        
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model cannot be empty");
        }
        
        if (gigabyte <= 0 || gigabyte > 4000) {
            throw new IllegalArgumentException("Hard disk capacity must be between 1 and 4000 GB");
        }
        
        if (ram <= 0 || ram > 128) {
            throw new IllegalArgumentException("RAM must be between 1 and 128 GB");
        }
        
        if (performanceType == null) {
            throw new IllegalArgumentException("Performance type cannot be null");
        }
    }

    // Getters

    public UUID getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public int getGigabyte() {
        return gigabyte;
    }

    public int getRam() {
        return ram;
    }

    public PerformanceTypeEnum getPerformanceType() {
        return performanceType;
    }

    public LaptopState getState() {
        return state;
    }

    // Setters

    public void setBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            throw new IllegalArgumentException("Brand cannot be empty");
        }
        
        String oldValue = this.brand;
        this.brand = brand;
    }

    public void setModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model cannot be empty");
        }
        
        String oldValue = this.model;
        this.model = model;
    }

    public void setGigabyte(int gigabyte) {
        if (gigabyte <= 0 || gigabyte > 4000) {
            throw new IllegalArgumentException("Hard disk capacity must be between 1 and 4000 GB");
        }
        
        int oldValue = this.gigabyte;
        this.gigabyte = gigabyte;
    }

    public void setRam(int ram) {
        if (ram <= 0 || ram > 128) {
            throw new IllegalArgumentException("RAM must be between 1 and 128 GB");
        }
        
        int oldValue = this.ram;
        this.ram = ram;
    }

    public void setPerformanceType(PerformanceTypeEnum performanceType) {
        if (performanceType == null) {
            throw new IllegalArgumentException("Performance type cannot be null");
        }
        
        PerformanceTypeEnum oldValue = this.performanceType;
        this.performanceType = performanceType;
    }


    public String getStateClassSimpleName() {
        return state.getClass().getSimpleName();
    }


    public boolean isAvailable() {
        return state instanceof AvailableState;
    }


    public boolean isLoaned() {
        return state instanceof LoanedState;
    }


    public void changeState(LaptopState newState) {
        LaptopState oldState = this.state;
        String oldStateName = oldState.getSimpleName();
        boolean wasAvailable = this.isAvailable();

        this.state = newState;

        String newStateName = newState.getSimpleName();
        boolean isNowAvailable = this.isAvailable();

        support.firePropertyChange(EVENT_STATE_CHANGED, oldStateName, newStateName);

        // Only fire availability event if it actually changed
        if (wasAvailable != isNowAvailable) {
            support.firePropertyChange(EVENT_AVAILABILITY_CHANGED, wasAvailable, isNowAvailable);
        }
    }


    public void setStateFromDatabase(String stateName) {
        if ("LoanedState".equals(stateName)) {
            if (!(state instanceof LoanedState)) {
                changeState(new LoanedState());
            }
        } else {
            if (!(state instanceof AvailableState)) {
                changeState(new AvailableState());
            }
        }
    }

    public void setState(){
        state.click(this);
    }

    @Override
    public String toString() {
        return brand + " " + model + " (" + performanceType + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Laptop laptop = (Laptop) o;
        return Objects.equals(id, laptop.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    // Observer add / remove listener metoder

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