package objects;

import enums.PerformanceTypeEnum;
import util.PropertyChangeSubjectInterface;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;


public class Laptop implements PropertyChangeSubjectInterface, Serializable {
    // Add serialVersionUID for version control of serialization
    private static final long serialVersionUID = 1L;

    private UUID id;
    private String brand;
    private String model;
    private int gigabyte;
    private int ram;
    private PerformanceTypeEnum performanceType;
    private transient LaptopState state; // Mark as transient since LaptopState might not be serializable
    private transient PropertyChangeSupport support; // PropertyChangeSupport is not serializable

    public static final String EVENT_BRAND_CHANGED = "laptop_brand_changed";
    public static final String EVENT_MODEL_CHANGED = "laptop_model_changed";
    public static final String EVENT_RAM_CHANGED = "laptop_ram_changed";
    public static final String EVENT_DISK_CHANGED = "laptop_disk_changed";
    public static final String EVENT_PERFORMANCE_CHANGED = "laptop_performance_changed";
    public static final String EVENT_STATE_CHANGED = "laptop_state_changed";
    public static final String EVENT_AVAILABILITY_CHANGED = "laptop_availability_changed";

    // Store state name for serialization
    private String stateName;

    public Laptop(String brand, String model, int gigabyte, int ram, PerformanceTypeEnum performanceType) {
        this(UUID.randomUUID(), brand, model, gigabyte, ram, performanceType);
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
        this.stateName = "AvailableState";
        this.support = new PropertyChangeSupport(this); // Initialize support in constructor
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
        // Ensure state is initialized after deserialization
        if (state == null) {
            initializeStateFromName();
        }
        return state;
    }

    // Setters

    public void setBrand(String brand) {
        if (brand == null || brand.trim().isEmpty()) {
            throw new IllegalArgumentException("Brand cannot be empty");
        }

        String oldValue = this.brand;
        this.brand = brand;
        if (support != null) {
            support.firePropertyChange(EVENT_BRAND_CHANGED, oldValue, brand);
        }
    }

    public void setModel(String model) {
        if (model == null || model.trim().isEmpty()) {
            throw new IllegalArgumentException("Model cannot be empty");
        }

        String oldValue = this.model;
        this.model = model;
        if (support != null) {
            support.firePropertyChange(EVENT_MODEL_CHANGED, oldValue, model);
        }
    }

    public void setGigabyte(int gigabyte) {
        if (gigabyte <= 0 || gigabyte > 4000) {
            throw new IllegalArgumentException("Hard disk capacity must be between 1 and 4000 GB");
        }

        int oldValue = this.gigabyte;
        this.gigabyte = gigabyte;
        if (support != null) {
            support.firePropertyChange(EVENT_DISK_CHANGED, oldValue, gigabyte);
        }
    }

    public void setRam(int ram) {
        if (ram <= 0 || ram > 128) {
            throw new IllegalArgumentException("RAM must be between 1 and 128 GB");
        }

        int oldValue = this.ram;
        this.ram = ram;
        if (support != null) {
            support.firePropertyChange(EVENT_RAM_CHANGED, oldValue, ram);
        }
    }

    public void setPerformanceType(PerformanceTypeEnum performanceType) {
        if (performanceType == null) {
            throw new IllegalArgumentException("Performance type cannot be null");
        }

        PerformanceTypeEnum oldValue = this.performanceType;
        this.performanceType = performanceType;
        if (support != null) {
            support.firePropertyChange(EVENT_PERFORMANCE_CHANGED, oldValue, performanceType);
        }
    }

    public String getStateClassSimpleName() {
        if (state == null) {
            initializeStateFromName();
        }
        return state.getClass().getSimpleName();
    }

    public boolean isAvailable() {
        if (state == null) {
            initializeStateFromName();
        }
        return state instanceof AvailableState;
    }

    public boolean isLoaned() {
        if (state == null) {
            initializeStateFromName();
        }
        return state instanceof LoanedState;
    }

    public void changeState(LaptopState newState) {
        if (newState == null) {
            throw new IllegalArgumentException("New state cannot be null");
        }

        LaptopState oldState = this.state;
        String oldStateName = oldState.getSimpleName();
        boolean wasAvailable = this.isAvailable();

        this.state = newState;
        this.stateName = newState.getClass().getSimpleName();

        String newStateName = newState.getSimpleName();
        boolean isNowAvailable = this.isAvailable();

        // Ensure support is not null before using it
        if (support != null) {
            support.firePropertyChange(EVENT_STATE_CHANGED, oldStateName, newStateName);

            // Only fire availability event if it actually changed
            if (wasAvailable != isNowAvailable) {
                support.firePropertyChange(EVENT_AVAILABILITY_CHANGED, wasAvailable, isNowAvailable);
            }
        }
    }

    public void setStateFromDatabase(String stateName) {
        this.stateName = stateName;

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
        if (state == null) {
            initializeStateFromName();
        }
        state.click(this);
    }

    // Helper method to initialize state after deserialization
    private void initializeStateFromName() {
        if ("LoanedState".equals(stateName)) {
            this.state = new LoanedState();
        } else {
            this.state = new AvailableState();
        }
    }

    // This method is called during deserialization
    private void readObject(java.io.ObjectInputStream in) throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject();
        // Initialize transient fields
        this.support = new PropertyChangeSupport(this);
        initializeStateFromName();
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

    // Observer add / remove listener methods

    @Override
    public void addListener(PropertyChangeListener listener) {
        if (support == null) {
            support = new PropertyChangeSupport(this);
        }
        support.addPropertyChangeListener(listener);
    }

    @Override
    public void removeListener(PropertyChangeListener listener) {
        if (support != null) {
            support.removePropertyChangeListener(listener);
        }
    }

    @Override
    public void addListener(String propertyName, PropertyChangeListener listener) {
        if (support == null) {
            support = new PropertyChangeSupport(this);
        }
        support.addPropertyChangeListener(propertyName, listener);
    }

    @Override
    public void removeListener(String propertyName, PropertyChangeListener listener) {
        if (support != null) {
            support.removePropertyChangeListener(propertyName, listener);
        }
    }
}
