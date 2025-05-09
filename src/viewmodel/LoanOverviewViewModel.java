package viewmodel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import alt.enums.PerformanceTypeEnum;
import alt.logic.DataModel;
import alt.models.Laptop;
import alt.models.Reservation;
import alt.models.Student;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

/**
 * ViewModel for visning af låneoversigt.
 * Håndterer data og logik for LoanOverviewView.
 */
public class LoanOverviewViewModel implements PropertyChangeListener {

  private final DataModel model;

  // Properties til binding med View
  private final IntegerProperty totalActiveLoans;
  private final IntegerProperty highPerformanceLoans;
  private final IntegerProperty lowPerformanceLoans;

  // ObservableLists til tabeller - bruger indre klasser i stedet for separate model-klasser
  private final ObservableList<LoanData> highPerformanceLoanList;
  private final ObservableList<LoanData> lowPerformanceLoanList;

  /**
   * Indre klasse til at indkapsle lån data til visning.
   */
  public static class LoanData {
    private final StringProperty reservationId = new SimpleStringProperty();
    private final StringProperty studentName = new SimpleStringProperty();
    private final StringProperty viaId = new SimpleStringProperty();
    private final StringProperty email = new SimpleStringProperty();
    private final StringProperty phoneNumber = new SimpleStringProperty();
    private final StringProperty laptopBrand = new SimpleStringProperty();
    private final StringProperty laptopModel = new SimpleStringProperty();
    private final StringProperty laptopSpecs = new SimpleStringProperty();
    private final StringProperty loanDate = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty();

    public LoanData(String reservationId, String studentName, String viaId, String email,
                    String phoneNumber, String laptopBrand, String laptopModel,
                    String laptopSpecs, String loanDate, String status) {
      this.reservationId.set(reservationId);
      this.studentName.set(studentName);
      this.viaId.set(viaId);
      this.email.set(email);
      this.phoneNumber.set(phoneNumber);
      this.laptopBrand.set(laptopBrand);
      this.laptopModel.set(laptopModel);
      this.laptopSpecs.set(laptopSpecs);
      this.loanDate.set(loanDate);
      this.status.set(status);
    }

    // Getter-metoder til properties
    public StringProperty reservationIdProperty() { return reservationId; }
    public StringProperty studentNameProperty() { return studentName; }
    public StringProperty viaIdProperty() { return viaId; }
    public StringProperty emailProperty() { return email; }
    public StringProperty phoneNumberProperty() { return phoneNumber; }
    public StringProperty laptopBrandProperty() { return laptopBrand; }
    public StringProperty laptopModelProperty() { return laptopModel; }
    public StringProperty laptopSpecsProperty() { return laptopSpecs; }
    public StringProperty loanDateProperty() { return loanDate; }
    public StringProperty statusProperty() { return status; }
  }

  /**
   * Konstruktør for LoanOverviewViewModel.
   *
   * @param model Reference til DataModel interfacet
   */
  public LoanOverviewViewModel(DataModel model) {
    this.model = model;

    // Initialiser properties
    totalActiveLoans = new SimpleIntegerProperty(0);
    highPerformanceLoans = new SimpleIntegerProperty(0);
    lowPerformanceLoans = new SimpleIntegerProperty(0);

    // Initialiser observableLists
    highPerformanceLoanList = FXCollections.observableArrayList();
    lowPerformanceLoanList = FXCollections.observableArrayList();

    // Registrer som listener på model for at modtage opdateringer
    model.addPropertyChangeListener(this);

    // Initialiser data
    refreshData();
  }

  /**
   * Opdaterer data i ViewModel fra modellen.
   */
  public void refreshData() {
    updateLoanLists();
    updateCounts();
  }

  /**
   * Opdaterer lån lister fra model data.
   */
  private void updateLoanLists() {
    // Hent alle aktive reservationer fra model
    List<Reservation> activeReservations = model.getAllActiveReservations();

    // Ryd eksisterende lister
    highPerformanceLoanList.clear();
    lowPerformanceLoanList.clear();

    // Konverter aktive reservationer til viewmodels og kategoriser dem
    for (Reservation reservation : activeReservations) {
      LoanData loanData = convertToLoanData(reservation);

      Laptop laptop = reservation.getLaptop();
      if (laptop.getPerformanceType() == PerformanceTypeEnum.HIGH) {
        highPerformanceLoanList.add(loanData);
      } else {
        lowPerformanceLoanList.add(loanData);
      }
    }
  }

  /**
   * Opdaterer tællere baseret på fyldte lister.
   */
  private void updateCounts() {
    highPerformanceLoans.set(highPerformanceLoanList.size());
    lowPerformanceLoans.set(lowPerformanceLoanList.size());
    totalActiveLoans.set(highPerformanceLoans.get() + lowPerformanceLoans.get());
  }

  /**
   * Konverterer en model Reservation til en LoanData.
   */
  private LoanData convertToLoanData(Reservation reservation) {
    // Hent data fra reservation
    String reservationId = reservation.getReservationId().toString();

    // Student info
    Student student = reservation.getStudent();
    String studentName = student.getName();
    String viaId = String.valueOf(student.getViaId());
    String email = student.getEmail();
    String phoneNumber = String.valueOf(student.getPhoneNumber());

    // Laptop info
    Laptop laptop = reservation.getLaptop();
    String laptopBrand = laptop.getBrand();
    String laptopModel = laptop.getModel();
    String laptopSpecs = laptop.getRam() + "GB RAM, " + laptop.getGigabyte() + "GB HDD";

    // Dato og status
    String loanDate = reservation.getCreationDate().toString();
    String status = reservation.getStatus().getDisplayName();

    return new LoanData(reservationId, studentName, viaId, email, phoneNumber,
            laptopBrand, laptopModel, laptopSpecs, loanDate, status);
  }

  /**
   * Håndterer propertyChange events fra model.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String propertyName = evt.getPropertyName();

    if ("reservationCreated".equals(propertyName) ||
            "reservationStatusUpdated".equals(propertyName) ||
            "activeReservationsCount".equals(propertyName) ||
            "laptopStateChanged".equals(propertyName)) {

      refreshData();
    }
  }

  /**
   * Afslutter applikationen.
   * Oprydning, frigørelse af ressourcer osv.
   */
  public void closeApplication() {
    // Fjern denne ViewModel som listener
    model.removePropertyChangeListener(this);

    // Andre handlinger der er nødvendige inden lukning
  }

  // Getter metoder til properties og observable lists

  public IntegerProperty totalActiveLoansProperty() {
    return totalActiveLoans;
  }

  public IntegerProperty highPerformanceLoansProperty() {
    return highPerformanceLoans;
  }

  public IntegerProperty lowPerformanceLoansProperty() {
    return lowPerformanceLoans;
  }

  public ObservableList<LoanData> getHighPerformanceLoans() {
    return highPerformanceLoanList;
  }

  public ObservableList<LoanData> getLowPerformanceLoans() {
    return lowPerformanceLoanList;
  }
}