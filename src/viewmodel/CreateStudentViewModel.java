package viewmodel;

import enums.PerformanceTypeEnum;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.Model;
import model.ModelImpl;
import objects.Laptop;
import objects.Reservation;
import objects.Student;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * ViewModel for creating and assigning laptops to students.
 */
public class CreateStudentViewModel implements PropertyChangeListener {
    private Model model;

    // Input fields
    private StringProperty nameProperty;
    private StringProperty emailProperty;
    private StringProperty viaIdProperty;
    private StringProperty phoneNumberProperty;
    private StringProperty degreeTitleProperty;
    private ObjectProperty<LocalDate> degreeEndDateProperty;
    private BooleanProperty highPerformanceProperty;

    // Result labels
    private StringProperty resultStudentProperty;
    private StringProperty resultStatusProperty;
    private StringProperty resultLaptopProperty;
    private StringProperty errorProperty;

    // Tables data
    private ObservableList<Laptop> highPerformanceLaptops;
    private ObservableList<Laptop> lowPerformanceLaptops;
    private ObservableList<Student> highPerformanceQueue;
    private ObservableList<Student> lowPerformanceQueue;

    /**
     * Constructs a new CreateStudentViewModel.
     *
     * @param model The model providing business logic and data
     */
    public CreateStudentViewModel(Model model) {
        this.model = model;

        // Initialize properties
        nameProperty = new SimpleStringProperty("");
        emailProperty = new SimpleStringProperty("");
        viaIdProperty = new SimpleStringProperty("");
        phoneNumberProperty = new SimpleStringProperty("");
        degreeTitleProperty = new SimpleStringProperty("");
        degreeEndDateProperty = new SimpleObjectProperty<>(LocalDate.now().plusYears(3)); // Default to 3 years from now
        highPerformanceProperty = new SimpleBooleanProperty(false);

        resultStudentProperty = new SimpleStringProperty("Ingen handling endnu");
        resultStatusProperty = new SimpleStringProperty("Ingen handling endnu");
        resultLaptopProperty = new SimpleStringProperty("Ingen tildeling endnu");
        errorProperty = new SimpleStringProperty("");

        // Initialize observable lists
        highPerformanceLaptops = FXCollections.observableArrayList();
        lowPerformanceLaptops = FXCollections.observableArrayList();
        highPerformanceQueue = FXCollections.observableArrayList();
        lowPerformanceQueue = FXCollections.observableArrayList();

        // Register as listener to model events
        model.addListener(this);

        // Load initial data
        refreshLaptops();
        refreshQueues();
    }

    /**
     * Creates a new student and attempts to assign a laptop.
     *
     * @return True if student was created, false otherwise
     */
    public boolean createStudent() {
        try {
            // Clear error message
            errorProperty.set("");

            // Validate inputs
            if (nameProperty.get().trim().isEmpty()) {
                errorProperty.set("Navn skal udfyldes");
                return false;
            }

            if (emailProperty.get().trim().isEmpty() || !isValidEmail(emailProperty.get())) {
                errorProperty.set("En gyldig e-mail skal angives");
                return false;
            }

            if (viaIdProperty.get().trim().isEmpty() || !isValidViaId(viaIdProperty.get())) {
                errorProperty.set("Et gyldigt VIA ID skal angives (4-8 cifre)");
                return false;
            }

            if (phoneNumberProperty.get().trim().isEmpty() || !isValidPhoneNumber(phoneNumberProperty.get())) {
                errorProperty.set("Et gyldigt telefonnummer skal angives (8-12 cifre)");
                return false;
            }

            if (degreeTitleProperty.get().trim().isEmpty()) {
                errorProperty.set("Uddannelsestitel skal udfyldes");
                return false;
            }

            if (degreeEndDateProperty.get() == null) {
                errorProperty.set("Uddannelsesslutdato skal angives");
                return false;
            }

            // Convert LocalDate to Date
            Date degreeEndDate = Date.from(degreeEndDateProperty.get().atStartOfDay(ZoneId.systemDefault()).toInstant());

            // Get performance type
            PerformanceTypeEnum performanceType = highPerformanceProperty.get() ?
                    PerformanceTypeEnum.HIGH : PerformanceTypeEnum.LOW;

            // Parse numeric fields
            int viaId;
            int phoneNumber;

            try {
                viaId = Integer.parseInt(viaIdProperty.get().trim());
            } catch (NumberFormatException e) {
                errorProperty.set("VIA ID skal være et tal");
                return false;
            }

            try {
                phoneNumber = Integer.parseInt(phoneNumberProperty.get().trim());
            } catch (NumberFormatException e) {
                errorProperty.set("Telefonnummer skal være et tal");
                return false;
            }

            // Create student
            Student student = model.createStudent(
                    nameProperty.get().trim(),
                    degreeEndDate,
                    degreeTitleProperty.get().trim(),
                    viaId,
                    emailProperty.get().trim(),
                    phoneNumber,
                    performanceType
            );

            if (student == null) {
                errorProperty.set("Fejl ved oprettelse af student. Måske findes VIA ID allerede?");
                return false;
            }

            // Update result fields
            resultStudentProperty.set(student.getName() + " (VIA ID: " + student.getViaId() + ")");

            // Try to assign laptop
            Laptop laptop = model.seeNextAvailableLaptop(performanceType);

            if (laptop != null) {
                // Laptop available, create reservation
                Reservation reservation = model.createReservation(student, laptop);

                if (reservation != null) {
                    resultStatusProperty.set("Computer tildelt");
                    resultLaptopProperty.set(laptop.getBrand() + " " + laptop.getModel());
                } else {
                    resultStatusProperty.set("Fejl ved tildeling af computer");
                    resultLaptopProperty.set("Ingen tildeling");

                    // Add to queue
                    if (performanceType == PerformanceTypeEnum.HIGH) {
                        model.addToHighPerformanceQueue(student.getViaId());
                    } else {
                        model.addToLowPerformanceQueue(student.getViaId());
                    }
                }
            } else {
                // No laptop available, add to queue
                resultStatusProperty.set("Tilføjet til venteliste");
                resultLaptopProperty.set("Ingen ledige computere");

                if (performanceType == PerformanceTypeEnum.HIGH) {
                    model.addToHighPerformanceQueue(student.getViaId());
                } else {
                    model.addToLowPerformanceQueue(student.getViaId());
                }
            }

            // Refresh data
            refreshLaptops();
            refreshQueues();

            // Clear form
            clearForm();

            return true;
        } catch (Exception e) {
            errorProperty.set("Fejl: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Clears the form fields.
     */
    public void clearForm() {
        nameProperty.set("");
        emailProperty.set("");
        viaIdProperty.set("");
        phoneNumberProperty.set("");
        degreeTitleProperty.set("");
        degreeEndDateProperty.set(LocalDate.now().plusYears(3));
        highPerformanceProperty.set(false);
        errorProperty.set("");
    }

    /**
     * Refreshes the laptop lists.
     */
    public void refreshLaptops() {
        // Clear lists
        highPerformanceLaptops.clear();
        lowPerformanceLaptops.clear();

        // Get laptops from model
        List<Laptop> allLaptops = model.getAllLaptops();

        // Sort into high/low performance
        for (Laptop laptop : allLaptops) {
            if (laptop.getPerformanceType() == PerformanceTypeEnum.HIGH) {
                highPerformanceLaptops.add(laptop);
            } else {
                lowPerformanceLaptops.add(laptop);
            }
        }
    }

    /**
     * Refreshes the queue lists.
     */
    public void refreshQueues() {
        // Use client interface to get queues
        List<Student> highQueue = model.getClient().getHighPerformanceQueue();
        List<Student> lowQueue = model.getClient().getLowPerformanceQueue();

        // Update observable lists
        highPerformanceQueue.clear();
        if (highQueue != null) {
            highPerformanceQueue.addAll(highQueue);
        }

        lowPerformanceQueue.clear();
        if (lowQueue != null) {
            lowPerformanceQueue.addAll(lowQueue);
        }
    }

    // Validation helpers

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidViaId(String viaId) {
        return viaId != null && viaId.matches("^[0-9]{4,8}$");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^[0-9]{8,12}$");
    }

    // Property getters

    public StringProperty nameProperty() {
        return nameProperty;
    }

    public StringProperty emailProperty() {
        return emailProperty;
    }

    public StringProperty viaIdProperty() {
        return viaIdProperty;
    }

    public StringProperty phoneNumberProperty() {
        return phoneNumberProperty;
    }

    public StringProperty degreeTitleProperty() {
        return degreeTitleProperty;
    }

    public ObjectProperty<LocalDate> degreeEndDateProperty() {
        return degreeEndDateProperty;
    }

    public BooleanProperty highPerformanceProperty() {
        return highPerformanceProperty;
    }

    public StringProperty resultStudentProperty() {
        return resultStudentProperty;
    }

    public StringProperty resultStatusProperty() {
        return resultStatusProperty;
    }

    public StringProperty resultLaptopProperty() {
        return resultLaptopProperty;
    }

    public StringProperty errorProperty() {
        return errorProperty;
    }

    public ObservableList<Laptop> getHighPerformanceLaptops() {
        return highPerformanceLaptops;
    }

    public ObservableList<Laptop> getLowPerformanceLaptops() {
        return lowPerformanceLaptops;
    }

    public ObservableList<Student> getHighPerformanceQueue() {
        return highPerformanceQueue;
    }

    public ObservableList<Student> getLowPerformanceQueue() {
        return lowPerformanceQueue;
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
                propertyName.equals(ModelImpl.EVENT_LAPTOP_STATE_CHANGED) ||
                propertyName.equals(ModelImpl.EVENT_LAPTOP_UPDATED)) {

            refreshLaptops();
        } else if (propertyName.equals(ModelImpl.EVENT_STUDENT_CREATED) ||
                propertyName.equals(ModelImpl.EVENT_STUDENT_DELETED) ||
                propertyName.equals(ModelImpl.EVENT_STUDENT_UPDATED)) {

            refreshQueues();
        }
    }
}