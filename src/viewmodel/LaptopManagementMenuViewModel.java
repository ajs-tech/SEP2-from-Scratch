package viewmodel;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import model.Model;
import model.ModelImpl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * ViewModel for the main menu screen of the laptop management application.
 * Provides data for the view to display and handles user interactions.
 */
public class LaptopManagementMenuViewModel implements PropertyChangeListener {
    private Model model;

    // Observable properties for binding with view
    private SimpleStringProperty statusProperty;
    private SimpleIntegerProperty availableLaptopsProperty;
    private SimpleIntegerProperty loanedLaptopsProperty;


    public LaptopManagementMenuViewModel(Model modelImp) {
        this.model = modelImp;
        this.statusProperty = new SimpleStringProperty("Systemstatus: Online");
        this.availableLaptopsProperty = new SimpleIntegerProperty(0);
        this.loanedLaptopsProperty = new SimpleIntegerProperty(0);

        // Listen for changes in the model
        model.addListener(this);

        // Initialize counts
        updateCounts();
    }

    /**
     * Updates the laptop counts from the model.
     */
    private void updateCounts() {
        availableLaptopsProperty.set(model.getAvailableLaptops().size());
        loanedLaptopsProperty.set(model.getLoanedLaptops().size());
    }

    /**
     * Gets the system status property.
     *
     * @return SimpleStringProperty containing system status
     */
    public SimpleStringProperty statusProperty() {
        return statusProperty;
    }

    /**
     * Gets the available laptops count property.
     *
     * @return SimpleIntegerProperty containing available laptops count
     */
    public SimpleIntegerProperty availableLaptopsProperty() {
        return availableLaptopsProperty;
    }

    /**
     * Gets the loaned laptops count property.
     *
     * @return SimpleIntegerProperty containing loaned laptops count
     */
    public SimpleIntegerProperty loanedLaptopsProperty() {
        return loanedLaptopsProperty;
    }

    /**
     * Returns the status string.
     *
     * @return Current system status
     */
    public String getStatus() {
        return statusProperty.get();
    }

    /**
     * Returns the available laptops count.
     *
     * @return Number of available laptops
     */
    public int getAvailableLaptopsCount() {
        return availableLaptopsProperty.get();
    }

    /**
     * Returns the loaned laptops count.
     *
     * @return Number of loaned laptops
     */
    public int getLoanedLaptopsCount() {
        return loanedLaptopsProperty.get();
    }

    /**
     * Handles property change events from the model.
     *
     * @param evt The property change event
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();

        // Update counts when laptop related events occur
        if (propertyName.equals(ModelImpl.EVENT_LAPTOP_CREATED) ||
                propertyName.equals(ModelImpl.EVENT_LAPTOP_CREATED) ||
                propertyName.equals(ModelImpl.EVENT_LAPTOP_CREATED) ||
                propertyName.equals(ModelImpl.EVENT_LAPTOP_CREATED) ||
                propertyName.equals(ModelImpl.EVENT_LAPTOP_CREATED) ||
                propertyName.equals(ModelImpl.EVENT_RESERVATION_CREATED) ||
                propertyName.equals(ModelImpl.EVENT_RESERVATION_COMPLETED) ||
                propertyName.equals(ModelImpl.EVENT_RESERVATION_CANCELLED)) {

            updateCounts();
        }
    }
}