package viewmodel;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.enums.PerformanceTypeEnum;
import model.logic.DataModel;
import model.models.Laptop;
import model.models.Reservation;
import model.models.Student;
import viewmodel.model.LoanViewModel;

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

  // ObservableLists til tabeller
  private final ObservableList<LoanViewModel> highPerformanceLoanList;
  private final ObservableList<LoanViewModel> lowPerformanceLoanList;

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
      LoanViewModel loanVM = convertToLoanViewModel(reservation);

      Laptop laptop = reservation.getLaptop();
      if (laptop.getPerformanceType() == PerformanceTypeEnum.HIGH) {
        highPerformanceLoanList.add(loanVM);
      } else {
        lowPerformanceLoanList.add(loanVM);
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
   * Konverterer en model Reservation til en LoanViewModel.
   */
  private LoanViewModel convertToLoanViewModel(Reservation reservation) {
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

    return new LoanViewModel(reservationId, studentName, viaId, email, phoneNumber,
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

  public ObservableList<LoanViewModel> getHighPerformanceLoans() {
    return highPerformanceLoanList;
  }

  public ObservableList<LoanViewModel> getLowPerformanceLoans() {
    return lowPerformanceLoanList;
  }
}