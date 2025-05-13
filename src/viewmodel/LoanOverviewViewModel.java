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
import objects.Reservation;
import objects.Student;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * ViewModel for the loan overview screen.
 */
public class LoanOverviewViewModel implements PropertyChangeListener {
    private Model model;

    // Observable properties for statistics
    private IntegerProperty activeLoansCountProperty;
    private IntegerProperty highPerformanceCountProperty;
    private IntegerProperty lowPerformanceCountProperty;

    // Observable lists for tables
    private ObservableList<LoanTableItem> highPerformanceLoans;
    private ObservableList<LoanTableItem> lowPerformanceLoans;

    // Status message
    private StringProperty statusProperty;

    /**
     * Constructs a new LoanOverviewViewModel.
     *
     * @param model The model providing business logic and data
     */
    public LoanOverviewViewModel(Model model) {
        this.model = model;

        // Initialize properties
        activeLoansCountProperty = new SimpleIntegerProperty(0);
        highPerformanceCountProperty = new SimpleIntegerProperty(0);
        lowPerformanceCountProperty = new SimpleIntegerProperty(0);
        statusProperty = new SimpleStringProperty("Klar til at vise udlånsoversigt");

        // Initialize observable lists
        highPerformanceLoans = FXCollections.observableArrayList();
        lowPerformanceLoans = FXCollections.observableArrayList();

        // Register as listener to model events
        model.addListener(this);

        // Load initial data
        refreshLoanOverview();
    }

    /**
     * Refreshes the loan overview tables and statistics.
     */
    public void refreshLoanOverview() {
        // Clear lists
        highPerformanceLoans.clear();
        lowPerformanceLoans.clear();

        // Counters for statistics
        int highPerformanceCount = 0;
        int lowPerformanceCount = 0;

        try {
            // Get active reservations from model
            List<Reservation> activeReservations = model.getActiveReservations();

            if (activeReservations != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                for (Reservation reservation : activeReservations) {
                    Student student = reservation.getStudent();
                    Laptop laptop = reservation.getLaptop();

                    // Create table item
                    LoanTableItem item = new LoanTableItem(
                            student.getName(),
                            String.valueOf(student.getViaId()),
                            student.getEmail(),
                            String.valueOf(student.getPhoneNumber()),
                            laptop.getBrand(),
                            laptop.getModel(),
                            laptop.getRam() + "GB RAM, " + laptop.getGigabyte() + "GB Disk",
                            dateFormat.format(reservation.getCreationDate())
                    );

                    // Add to appropriate list based on laptop's performance type
                    if (laptop.getPerformanceType() == PerformanceTypeEnum.HIGH) {
                        highPerformanceLoans.add(item);
                        highPerformanceCount++;
                    } else {
                        lowPerformanceLoans.add(item);
                        lowPerformanceCount++;
                    }
                }
            }

            // Update statistics
            int totalActive = highPerformanceCount + lowPerformanceCount;
            activeLoansCountProperty.set(totalActive);
            highPerformanceCountProperty.set(highPerformanceCount);
            lowPerformanceCountProperty.set(lowPerformanceCount);

            // Update status
            statusProperty.set("Oversigt opdateret - " + totalActive + " aktive udlån");

        } catch (Exception e) {
            statusProperty.set("Fejl ved opdatering af oversigt: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Property getters

    public IntegerProperty activeLoansCountProperty() {
        return activeLoansCountProperty;
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

    public ObservableList<LoanTableItem> getHighPerformanceLoans() {
        return highPerformanceLoans;
    }

    public ObservableList<LoanTableItem> getLowPerformanceLoans() {
        return lowPerformanceLoans;
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
        if (propertyName.equals(ModelImpl.EVENT_RESERVATION_CREATED) ||
                propertyName.equals(ModelImpl.EVENT_RESERVATION_COMPLETED) ||
                propertyName.equals(ModelImpl.EVENT_RESERVATION_CANCELLED) ||
                propertyName.equals(ModelImpl.EVENT_ACTIVE_RESERVATIONS_CHANGED)) {

            refreshLoanOverview();
        }
    }

    /**
     * Table item class for loan tables.
     */
    public static class LoanTableItem {
        private final String studentName;
        private final String viaId;
        private final String email;
        private final String phone;
        private final String laptopBrand;
        private final String laptopModel;
        private final String specs;
        private final String loanDate;

        public LoanTableItem(String studentName, String viaId, String email, String phone,
                             String laptopBrand, String laptopModel, String specs, String loanDate) {
            this.studentName = studentName;
            this.viaId = viaId;
            this.email = email;
            this.phone = phone;
            this.laptopBrand = laptopBrand;
            this.laptopModel = laptopModel;
            this.specs = specs;
            this.loanDate = loanDate;
        }

        public String getStudentName() {
            return studentName;
        }

        public String getViaId() {
            return viaId;
        }

        public String getEmail() {
            return email;
        }

        public String getPhone() {
            return phone;
        }

        public String getLaptopBrand() {
            return laptopBrand;
        }

        public String getLaptopModel() {
            return laptopModel;
        }

        public String getSpecs() {
            return specs;
        }

        public String getLoanDate() {
            return loanDate;
        }
    }
}