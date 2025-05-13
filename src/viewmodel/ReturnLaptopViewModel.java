package viewmodel;

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
import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel for returning laptops functionality.
 */
public class ReturnLaptopViewModel implements PropertyChangeListener {
    private Model model;

    // Properties for the search
    private StringProperty searchTypeProperty;
    private StringProperty searchTermProperty;
    private StringProperty searchErrorProperty;

    // Properties for the selected loan
    private StringProperty selectedViaIdProperty;
    private StringProperty selectedStudentNameProperty;
    private StringProperty selectedEmailProperty;
    private StringProperty selectedPhoneProperty;
    private StringProperty selectedLaptopBrandProperty;
    private StringProperty selectedLaptopModelProperty;
    private StringProperty selectedLaptopSpecsProperty;
    private StringProperty selectedLoanDateProperty;

    // Properties for the return result
    private StringProperty resultStudentProperty;
    private StringProperty resultComputerProperty;
    private StringProperty resultDateProperty;
    private StringProperty resultStatusProperty;

    // Property for general status
    private StringProperty statusProperty;

    // Observable list for active loans table
    private ObservableList<LoanTableItem> activeLoans;

    // Selected loan for return
    private Reservation selectedReservation;

    /**
     * Constructs a new ReturnLaptopViewModel.
     *
     * @param model The model providing business logic and data
     */
    public ReturnLaptopViewModel(Model model) {
        this.model = model;

        // Initialize properties
        searchTypeProperty = new SimpleStringProperty("VIA ID");
        searchTermProperty = new SimpleStringProperty("");
        searchErrorProperty = new SimpleStringProperty("");

        selectedViaIdProperty = new SimpleStringProperty("");
        selectedStudentNameProperty = new SimpleStringProperty("");
        selectedEmailProperty = new SimpleStringProperty("");
        selectedPhoneProperty = new SimpleStringProperty("");
        selectedLaptopBrandProperty = new SimpleStringProperty("");
        selectedLaptopModelProperty = new SimpleStringProperty("");
        selectedLaptopSpecsProperty = new SimpleStringProperty("");
        selectedLoanDateProperty = new SimpleStringProperty("");

        resultStudentProperty = new SimpleStringProperty("Ingen returnering endnu");
        resultComputerProperty = new SimpleStringProperty("Ingen returnering endnu");
        resultDateProperty = new SimpleStringProperty("Ingen returnering endnu");
        resultStatusProperty = new SimpleStringProperty("Ingen returnering endnu");

        statusProperty = new SimpleStringProperty("Klar til at søge efter studerende");

        // Initialize observable list
        activeLoans = FXCollections.observableArrayList();

        // Register as listener to model events
        model.addListener(this);

        // Load initial data
        refreshLoanList();
    }

    /**
     * Searches for a student based on the selected search type and term.
     */
    public void searchStudent() {
        // Clear error
        searchErrorProperty.set("");

        // Validate search term
        if (searchTermProperty.get() == null || searchTermProperty.get().trim().isEmpty()) {
            searchErrorProperty.set("Søgeterm skal udfyldes");
            return;
        }

        String searchType = searchTypeProperty.get();
        String searchTerm = searchTermProperty.get().trim();

        // Filter active loans based on search type and term
        List<LoanTableItem> filteredLoans = new ArrayList<>();

        for (LoanTableItem item : activeLoans) {
            boolean match = false;

            if (searchType.equals("VIA ID")) {
                match = item.getViaId().contains(searchTerm);
            } else if (searchType.equals("Telefonnummer")) {
                match = item.getPhone().contains(searchTerm);
            } else if (searchType.equals("Navn")) {
                match = item.getStudentName().toLowerCase().contains(searchTerm.toLowerCase());
            }

            if (match) {
                filteredLoans.add(item);
            }
        }

        // Update active loans list
        activeLoans.clear();
        activeLoans.addAll(filteredLoans);

        // Update status
        if (filteredLoans.isEmpty()) {
            statusProperty.set("Ingen resultater fundet for søgningen");
        } else {
            statusProperty.set("Fandt " + filteredLoans.size() + " resultater");
        }
    }

    /**
     * Clears the search and reloads all active loans.
     */
    public void clearSearch() {
        searchTermProperty.set("");
        searchErrorProperty.set("");
        searchTypeProperty.set("VIA ID");
        refreshLoanList();
        statusProperty.set("Søgning ryddet");
    }

    /**
     * Refreshes the list of active loans.
     */
    public void refreshLoanList() {
        activeLoans.clear();

        try {
            // Get active reservations using model's new methods
            List<Reservation> reservations = model.getActiveReservations();

            if (reservations != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

                for (Reservation reservation : reservations) {
                    Student student = reservation.getStudent();
                    Laptop laptop = reservation.getLaptop();

                    // Create table item
                    LoanTableItem item = new LoanTableItem(
                            String.valueOf(student.getViaId()),
                            student.getName(),
                            student.getEmail(),
                            String.valueOf(student.getPhoneNumber()),
                            laptop.getBrand(),
                            laptop.getModel(),
                            laptop.getRam() + "GB RAM, " + laptop.getGigabyte() + "GB Disk",
                            dateFormat.format(reservation.getCreationDate()),
                            reservation  // Store full reservation for later use
                    );

                    activeLoans.add(item);
                }
            }

            statusProperty.set("Liste opdateret - " + activeLoans.size() + " aktive udlån");
        } catch (Exception e) {
            statusProperty.set("Fejl ved hentning af udlån: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sets the selected loan based on table selection.
     *
     * @param selectedItem The selected table item
     */
    public void setSelectedLoan(LoanTableItem selectedItem) {
        if (selectedItem == null) {
            // Clear selected properties
            selectedViaIdProperty.set("");
            selectedStudentNameProperty.set("");
            selectedEmailProperty.set("");
            selectedPhoneProperty.set("");
            selectedLaptopBrandProperty.set("");
            selectedLaptopModelProperty.set("");
            selectedLaptopSpecsProperty.set("");
            selectedLoanDateProperty.set("");
            selectedReservation = null;
            return;
        }

        // Set selected properties
        selectedViaIdProperty.set(selectedItem.getViaId());
        selectedStudentNameProperty.set(selectedItem.getStudentName());
        selectedEmailProperty.set(selectedItem.getEmail());
        selectedPhoneProperty.set(selectedItem.getPhone());
        selectedLaptopBrandProperty.set(selectedItem.getLaptopBrand());
        selectedLaptopModelProperty.set(selectedItem.getLaptopModel());
        selectedLaptopSpecsProperty.set(selectedItem.getLaptopSpecs());
        selectedLoanDateProperty.set(selectedItem.getLoanDate());

        // Store the reservation
        selectedReservation = selectedItem.getReservation();

        statusProperty.set("Valgte " + selectedItem.getStudentName() + " med " +
                selectedItem.getLaptopBrand() + " " + selectedItem.getLaptopModel());
    }

    /**
     * Returns the selected laptop.
     *
     * @return True if the laptop was successfully returned, false otherwise
     */
    public boolean returnComputer() {
        if (selectedReservation == null) {
            statusProperty.set("Ingen computer valgt til returnering");
            return false;
        }

        try {
            // Get student and laptop
            Student student = selectedReservation.getStudent();
            Laptop laptop = selectedReservation.getLaptop();

            // Complete the reservation using the model's completeReservation method
            boolean success = model.completeReservation(selectedReservation.getReservationId());

            if (success) {
                // Update result fields
                resultStudentProperty.set(student.getName() + " (VIA ID: " + student.getViaId() + ")");
                resultComputerProperty.set(laptop.getBrand() + " " + laptop.getModel());
                resultDateProperty.set(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new java.util.Date()));
                resultStatusProperty.set("Computer returneret");

                // Update status
                statusProperty.set("Computer returneret");

                // Refresh loan list
                refreshLoanList();

                return true;
            } else {
                statusProperty.set("Fejl ved returnering af computer");
                return false;
            }
        } catch (Exception e) {
            statusProperty.set("Fejl: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Property getters

    public StringProperty searchTypeProperty() {
        return searchTypeProperty;
    }

    public StringProperty searchTermProperty() {
        return searchTermProperty;
    }

    public StringProperty searchErrorProperty() {
        return searchErrorProperty;
    }

    public StringProperty resultStudentProperty() {
        return resultStudentProperty;
    }

    public StringProperty resultComputerProperty() {
        return resultComputerProperty;
    }

    public StringProperty resultDateProperty() {
        return resultDateProperty;
    }

    public StringProperty resultStatusProperty() {
        return resultStatusProperty;
    }

    public StringProperty statusProperty() {
        return statusProperty;
    }

    public ObservableList<LoanTableItem> getActiveLoans() {
        return activeLoans;
    }

    public Reservation getSelectedReservation() {
        return selectedReservation;
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

            refreshLoanList();
        }
    }

    /**
     * Table item class for loan list.
     */
    public static class LoanTableItem {
        private final String viaId;
        private final String studentName;
        private final String email;
        private final String phone;
        private final String laptopBrand;
        private final String laptopModel;
        private final String laptopSpecs;
        private final String loanDate;
        private final Reservation reservation;

        public LoanTableItem(String viaId, String studentName, String email, String phone,
                             String laptopBrand, String laptopModel, String laptopSpecs,
                             String loanDate, Reservation reservation) {
            this.viaId = viaId;
            this.studentName = studentName;
            this.email = email;
            this.phone = phone;
            this.laptopBrand = laptopBrand;
            this.laptopModel = laptopModel;
            this.laptopSpecs = laptopSpecs;
            this.loanDate = loanDate;
            this.reservation = reservation;
        }

        public String getViaId() {
            return viaId;
        }

        public String getStudentName() {
            return studentName;
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

        public String getLaptopSpecs() {
            return laptopSpecs;
        }

        public String getLoanDate() {
            return loanDate;
        }

        public Reservation getReservation() {
            return reservation;
        }
    }
}
