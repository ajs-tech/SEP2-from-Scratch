package viewmodel;

import enums.PerformanceTypeEnum;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.ModelImpl;
import objects.Laptop;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for displaying available laptops.
 */
public class AvailableLaptopsViewModel implements PropertyChangeListener {
    private Model model;

    // Observable properties for statistics
    private IntegerProperty totalAvailableCountProperty;
    private IntegerProperty highPerformanceCountProperty;
    private IntegerProperty lowPerformanceCountProperty;

    // Observable lists for tables
    private ObservableList<LaptopTableItem> highPerformanceLaptops;
    private ObservableList<LaptopTableItem> lowPerformanceLaptops;

    // Status message
    private StringProperty statusProperty;

    /**
     * Constructs a new AvailableLaptopsViewModel.
     *
     * @param model The model providing business logic and data
     */
    public AvailableLaptopsViewModel(Model model) {
        this.model = model;

        // Initialize properties
        totalAvailableCountProperty = new SimpleIntegerProperty(0);
        highPerformanceCountProperty = new SimpleIntegerProperty(0);
        lowPerformanceCountProperty = new SimpleIntegerProperty(0);
        statusProperty = new SimpleStringProperty("Klar til at vise tilgængelige computere");

        // Initialize observable lists
        highPerformanceLaptops = FXCollections.observableArrayList();
        lowPerformanceLaptops = FXCollections.observableArrayList();

        // Register as listener to model events
        model.addListener(this);

        // Load initial data
        refreshLaptops();
    }

    /**
     * Refreshes the laptop lists and statistics.
     */
    public void refreshLaptops() {
        // Clear lists
        highPerformanceLaptops.clear();
        lowPerformanceLaptops.clear();

        // Counters for statistics
        int highPerformanceCount = 0;
        int lowPerformanceCount = 0;

        try {
            // Get all laptops from model
            List<Laptop> allLaptops = model.getAllLaptops();

            if (allLaptops != null) {
                for (Laptop laptop : allLaptops) {
                    // Create table item
                    LaptopTableItem item = new LaptopTableItem(
                            laptop.getId().toString(),
                            laptop.getBrand(),
                            laptop.getModel(),
                            laptop.getRam(),
                            laptop.getGigabyte(),
                            laptop.getPerformanceType().toString(),
                            laptop.isAvailable() ? "Tilgængelig" : "Udlånt"
                    );

                    // Add to appropriate list based on performance type
                    if (laptop.getPerformanceType() == PerformanceTypeEnum.HIGH) {
                        highPerformanceLaptops.add(item);
                        if (laptop.isAvailable()) {
                            highPerformanceCount++;
                        }
                    } else {
                        lowPerformanceLaptops.add(item);
                        if (laptop.isAvailable()) {
                            lowPerformanceCount++;
                        }
                    }
                }
            }

            // Update statistics
            int totalAvailable = highPerformanceCount + lowPerformanceCount;
            totalAvailableCountProperty.set(totalAvailable);
            highPerformanceCountProperty.set(highPerformanceCount);
            lowPerformanceCountProperty.set(lowPerformanceCount);

            // Update status
            statusProperty.set("Oversigt opdateret - " + totalAvailable + " tilgængelige computere");

        } catch (Exception e) {
            statusProperty.set("Fejl ved opdatering af oversigt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Property getters

    public IntegerProperty totalAvailableCountProperty() {
        return totalAvailableCountProperty;
    }

    public IntegerProperty highPerformanceCountProperty() {
        return highPerformanceCountProperty;
    }

    public IntegerProperty lowPerformanceCountProperty() {
        return lowPerformanceCountProperty;
    }

    public StringProperty statusProperty() {
        return statusProperty;
    }

    public ObservableList<LaptopTableItem> getHighPerformanceLaptops() {
        return highPerformanceLaptops;
    }

    public ObservableList<LaptopTableItem> getLowPerformanceLaptops() {
        return lowPerformanceLaptops;
    }

    /**
     * Handles property change events from the model.
     *
     * @param evt The property change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        // Update when relevant events occur
        if (propertyName.equals(ModelImpl.EVENT_LAPTOP_CREATED) ||
                propertyName.equals(ModelImpl.EVENT_LAPTOP_DELETED) ||
                propertyName.equals(ModelImpl.EVENT_LAPTOP_UPDATED) ||
                propertyName.equals(ModelImpl.EVENT_LAPTOP_STATE_CHANGED) ||
                propertyName.equals(ModelImpl.EVENT_AVAILABLE_COUNT_CHANGED) ||
                propertyName.equals(ModelImpl.EVENT_LOANED_COUNT_CHANGED) ||
                propertyName.equals(ModelImpl.EVENT_RESERVATION_CREATED) ||
                propertyName.equals(ModelImpl.EVENT_RESERVATION_COMPLETED)) {

            refreshLaptops();
        }
    }

    /**
     * Table item class for laptop tables.
     */
    public static class LaptopTableItem {
        private final String id;
        private final String brand;
        private final String model;
        private final int ram;
        private final int disk;
        private final String performanceType;
        private final String status;

        public LaptopTableItem(String id, String brand, String model, int ram, int disk,
                               String performanceType, String status) {
            this.id = id;
            this.brand = brand;
            this.model = model;
            this.ram = ram;
            this.disk = disk;
            this.performanceType = performanceType;
            this.status = status;
        }

        public String getId() {
            return id;
        }

        public String getBrand() {
            return brand;
        }

        public String getModel() {
            return model;
        }

        public int getRam() {
            return ram;
        }

        public int getDisk() {
            return disk;
        }

        public String getPerformanceType() {
            return performanceType;
        }

        public String getStatus() {
            return status;
        }
    }
}