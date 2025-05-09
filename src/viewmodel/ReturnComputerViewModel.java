package viewmodel;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.enums.ReservationStatusEnum;
import model.logic.DataModel;
import model.models.Laptop;
import model.models.Reservation;
import model.models.Student;
import viewmodel.model.StudentLoanViewModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * ViewModel for returnering af laptops.
 * Håndterer data og logik for ReturnComputerView.
 */
public class ReturnComputerViewModel implements PropertyChangeListener {

  private final DataModel model;

  // Properties til binding med View
  private final StringProperty searchTerm;
  private final StringProperty searchType;
  private final StringProperty errorMessage;
  private final StringProperty resultStudent;
  private final StringProperty resultComputer;
  private final StringProperty resultDate;
  private final StringProperty resultStatus;
  private final StringProperty statusMessage;
  private final BooleanProperty returnButtonDisabled;

  // ObservableLists til tabeller
  private final ObservableList<StudentLoanViewModel> studentLoans;

  // Selected item
  private StudentLoanViewModel selectedLoan;

  /**
   * Konstruktør for ReturnComputerViewModel.
   *
   * @param model Reference til DataModel interfacet
   */
  public ReturnComputerViewModel(DataModel model) {
    this.model = model;

    // Initialiser properties
    searchTerm = new SimpleStringProperty("");
    searchType = new SimpleStringProperty("VIA ID");
    errorMessage = new SimpleStringProperty("");
    resultStudent = new SimpleStringProperty("Ingen returnering endnu");
    resultComputer = new SimpleStringProperty("Ingen returnering endnu");
    resultDate = new SimpleStringProperty("Ingen returnering endnu");
    resultStatus = new SimpleStringProperty("Ingen returnering endnu");
    statusMessage = new SimpleStringProperty("Klar til at søge efter studerende");
    returnButtonDisabled = new SimpleBooleanProperty(true);

    // Initialiser observableLists
    studentLoans = FXCollections.observableArrayList();

    // Selected item
    selectedLoan = null;

    // Registrer som listener på model for at modtage opdateringer
    model.addPropertyChangeListener(this);

    // Initialiser data
    refreshData();
  }

  /**
   * Opdaterer data i ViewModel fra modellen.
   */
  public void refreshData() {
    loadAllActiveLoans();
  }

  /**
   * Indlæser alle aktive lån til tabellen.
   */
  private void loadAllActiveLoans() {
    // Hent alle aktive reservationer fra model
    List<Reservation> activeReservations = model.getAllActiveReservations();

    // Ryd eksisterende liste
    studentLoans.clear();

    // Konverter aktive reservationer til viewmodels
    for (Reservation reservation : activeReservations) {
      StudentLoanViewModel loanVM = convertToStudentLoanViewModel(reservation);
      studentLoans.add(loanVM);
    }

    // Opdater status
    updateStatusMessage();
  }

  /**
   * Konverterer en model Reservation til en StudentLoanViewModel.
   */
  private StudentLoanViewModel convertToStudentLoanViewModel(Reservation reservation) {
    // Hent data fra reservation
    String reservationId = reservation.getReservationId().toString();

    // Student info
    Student student = reservation.getStudent();
    String viaId = String.valueOf(student.getViaId());
    String studentName = student.getName();
    String email = student.getEmail();
    String phoneNumber = String.valueOf(student.getPhoneNumber());

    // Laptop info
    Laptop laptop = reservation.getLaptop();
    String laptopId = laptop.getId().toString();
    String laptopBrand = laptop.getBrand();
    String laptopModel = laptop.getModel();
    String laptopSpecs = laptop.getRam() + "GB RAM, " + laptop.getGigabyte() + "GB HDD";

    // Dato
    String loanDate = reservation.getCreationDate().toString();

    return new StudentLoanViewModel(reservationId, viaId, studentName, email, phoneNumber,
            laptopId, laptopBrand, laptopModel, laptopSpecs, loanDate);
  }

  /**
   * Søger efter studerende baseret på input kriterier.
   *
   * @param searchTypeString Type af søgning (VIA ID, Telefonnummer eller Navn)
   */
  public void searchStudents(String searchTypeString) {
    // Ryd fejlbesked
    errorMessage.set("");

    // Tjek at søgefeltet ikke er tomt
    if (searchTerm.get() == null || searchTerm.get().trim().isEmpty()) {
      errorMessage.set("Søgefeltet må ikke være tomt");
      return;
    }

    // Gem søgetype
    searchType.set(searchTypeString);

    try {
      // Filtrér lån baseret på søgekriterier
      String term = searchTerm.get().trim().toLowerCase();

      // Ryd eksisterende lister
      ObservableList<StudentLoanViewModel> filteredList = FXCollections.observableArrayList();

      // Hent alle aktive reservationer igen for at sikre friske data
      List<Reservation> activeReservations = model.getAllActiveReservations();

      for (Reservation reservation : activeReservations) {
        Student student = reservation.getStudent();
        boolean matches = false;

        if ("VIA ID".equals(searchTypeString)) {
          matches = String.valueOf(student.getViaId()).contains(term);
        } else if ("Telefonnummer".equals(searchTypeString)) {
          matches = String.valueOf(student.getPhoneNumber()).contains(term);
        } else if ("Navn".equals(searchTypeString)) {
          matches = student.getName().toLowerCase().contains(term);
        }

        if (matches) {
          StudentLoanViewModel loanVM = convertToStudentLoanViewModel(reservation);
          filteredList.add(loanVM);
        }
      }

      // Opdater lista med filtrerede resultater
      studentLoans.clear();
      studentLoans.addAll(filteredList);

      // Opdater status message
      if (studentLoans.isEmpty()) {
        statusMessage.set("Ingen studerende fundet med de søgekriterier");
      } else {
        statusMessage.set("Fandt " + studentLoans.size() + " udlån. Vælg et for at returnere.");
      }

    } catch (Exception e) {
      errorMessage.set("Fejl ved søgning: " + e.getMessage());
      statusMessage.set("Fejl under søgning");
    }
  }

  /**
   * Rydder søgning og viser alle lån igen.
   */
  public void clearSearch() {
    searchTerm.set("");
    errorMessage.set("");
    loadAllActiveLoans();
  }

  /**
   * Opdaterer status besked baseret på antal lån.
   */
  private void updateStatusMessage() {
    if (studentLoans.isEmpty()) {
      statusMessage.set("Ingen aktive lån fundet");
    } else {
      statusMessage.set("Fandt " + studentLoans.size() + " aktive lån. Vælg et for at returnere.");
    }
  }

  /**
   * Sætter valgt lån.
   *
   * @param loan Det valgte StudentLoanViewModel, eller null hvis intet valgt
   */
  public void setSelectedLoan(StudentLoanViewModel loan) {
    selectedLoan = loan;
    returnButtonDisabled.set(selectedLoan == null);
  }

  /**
   * Returnerer den valgte laptop.
   */
  public void returnSelectedLaptop() {
    if (selectedLoan == null) {
      errorMessage.set("Ingen computer valgt til returnering");
      return;
    }

    try {
      // Find reservationen baseret på ID
      UUID reservationId = UUID.fromString(selectedLoan.getReservationId());

      // Markér reservationen som afsluttet
      boolean success = model.updateReservationStatus(reservationId, ReservationStatusEnum.COMPLETED);

      if (success) {
        // Opdater resultat information
        resultStudent.set(selectedLoan.getStudentName() + " (VIA ID: " + selectedLoan.getViaId() + ")");
        resultComputer.set(selectedLoan.getLaptopBrand() + " " + selectedLoan.getLaptopModel());
        resultDate.set(new Date().toString());
        resultStatus.set("Computer returneret succesfuldt");

        // Opdater status
        statusMessage.set("Computer returneret succesfuldt");

        // Ryd fejlbesked
        errorMessage.set("");

        // Opdater tabel
        refreshData();

        // Nulstil valgt lån
        selectedLoan = null;
        returnButtonDisabled.set(true);
      } else {
        errorMessage.set("Kunne ikke returnere computer - databasefejl");
        statusMessage.set("Fejl ved returnering af computer");
      }
    } catch (Exception e) {
      errorMessage.set("Fejl ved returnering: " + e.getMessage());
      statusMessage.set("Uventet fejl ved returnering");
    }
  }

  /**
   * Håndterer propertyChange events fra model.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    String propertyName = evt.getPropertyName();

    if ("reservationCreated".equals(propertyName) ||
            "reservationStatusUpdated".equals(propertyName) ||
            "activeReservationsCount".equals(propertyName)) {

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

  public StringProperty searchTermProperty() {
    return searchTerm;
  }

  public StringProperty searchTypeProperty() {
    return searchType;
  }

  public StringProperty errorMessageProperty() {
    return errorMessage;
  }

  public StringProperty resultStudentProperty() {
    return resultStudent;
  }

  public StringProperty resultComputerProperty() {
    return resultComputer;
  }

  public StringProperty resultDateProperty() {
    return resultDate;
  }

  public StringProperty resultStatusProperty() {
    return resultStatus;
  }

  public StringProperty statusMessageProperty() {
    return statusMessage;
  }

  public BooleanProperty returnButtonDisabledProperty() {
    return returnButtonDisabled;
  }

  public ObservableList<StudentLoanViewModel> getStudentLoans() {
    return studentLoans;
  }
}