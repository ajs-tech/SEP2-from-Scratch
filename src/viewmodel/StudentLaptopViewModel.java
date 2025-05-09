package viewmodel;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import model.enums.PerformanceTypeEnum;
import model.logic.DataModel;
import model.models.Laptop;
import model.models.Reservation;
import model.models.Student;
import viewmodel.model.LaptopViewModel;
import viewmodel.model.StudentViewModel;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

/**
 * ViewModel for student registrering og laptop tildeling.
 * Håndterer data og logik for StudentLaptopView.
 */
public class StudentLaptopViewModel implements PropertyChangeListener {

  private final DataModel model;

  // Properties til form binding
  private final StringProperty name;
  private final StringProperty email;
  private final StringProperty viaId;
  private final StringProperty phoneNumber;
  private final StringProperty degreeTitle;
  private final ObjectProperty<LocalDate> degreeEndDate;
  private final BooleanProperty lowPerformanceSelected;

  // Resultat og status properties
  private final StringProperty errorMessage;
  private final StringProperty resultStudent;
  private final StringProperty resultStatus;
  private final StringProperty resultLaptop;
  private final StringProperty statusMessage;

  // ObservableLists til tabeller
  private final ObservableList<LaptopViewModel> highPerformanceLaptops;
  private final ObservableList<LaptopViewModel> lowPerformanceLaptops;
  private final ObservableList<StudentViewModel> highPerformanceQueue;
  private final ObservableList<StudentViewModel> lowPerformanceQueue;

  /**
   * Konstruktør for StudentLaptopViewModel.
   *
   * @param model Reference til DataModel interfacet
   */
  public StudentLaptopViewModel(DataModel model) {
    this.model = model;

    // Initialiser form properties
    name = new SimpleStringProperty("");
    email = new SimpleStringProperty("");
    viaId = new SimpleStringProperty("");
    phoneNumber = new SimpleStringProperty("");
    degreeTitle = new SimpleStringProperty("");
    degreeEndDate = new SimpleObjectProperty<>();
    lowPerformanceSelected = new SimpleBooleanProperty(true);

    // Initialiser status properties
    errorMessage = new SimpleStringProperty("");
    resultStudent = new SimpleStringProperty("Ingen handling endnu");
    resultStatus = new SimpleStringProperty("Ingen handling endnu");
    resultLaptop = new SimpleStringProperty("Ingen tildeling endnu");
    statusMessage = new SimpleStringProperty("Klar til at oprette studerende");

    // Initialiser observableLists
    highPerformanceLaptops = FXCollections.observableArrayList();
    lowPerformanceLaptops = FXCollections.observableArrayList();
    highPerformanceQueue = FXCollections.observableArrayList();
    lowPerformanceQueue = FXCollections.observableArrayList();

    // Registrer som listener på model for at modtage opdateringer
    model.addPropertyChangeListener(this);

    // Initialiser data
    refreshData();
  }

  /**
   * Opdaterer al data i ViewModel fra modellen.
   */
  public void refreshData() {
    refreshLaptops();
    refreshQueues();
  }

  /**
   * Opdaterer kun laptop lister.
   */
  public void refreshLaptops() {
    updateLaptopLists();
  }

  /**
   * Opdaterer kun køer.
   */
  public void refreshQueues() {
    updateQueueLists();
  }

  /**
   * Opdaterer laptop lister fra model data.
   */
  private void updateLaptopLists() {
    // Hent alle laptops fra model
    List<Laptop> allLaptops = model.getAllLaptops();

    // Ryd eksisterende lister
    highPerformanceLaptops.clear();
    lowPerformanceLaptops.clear();

    // Konverter alle laptops til viewmodels og kategoriser dem
    for (Laptop laptop : allLaptops) {
      LaptopViewModel laptopVM = convertToLaptopViewModel(laptop);

      if (laptop.getPerformanceType() == PerformanceTypeEnum.HIGH) {
        highPerformanceLaptops.add(laptopVM);
      } else {
        lowPerformanceLaptops.add(laptopVM);
      }
    }
  }

  /**
   * Opdaterer køer fra model data.
   */
  private void updateQueueLists() {
    // Hent studerende fra køer
    List<Student> highQueue = model.getStudentsInHighPerformanceQueue();
    List<Student> lowQueue = model.getStudentsInLowPerformanceQueue();

    // Ryd eksisterende lister
    highPerformanceQueue.clear();
    lowPerformanceQueue.clear();

    // Konverter high performance kø til viewmodels
    for (Student student : highQueue) {
      StudentViewModel studentVM = convertToStudentViewModel(student);
      highPerformanceQueue.add(studentVM);
    }

    // Konverter low performance kø til viewmodels
    for (Student student : lowQueue) {
      StudentViewModel studentVM = convertToStudentViewModel(student);
      lowPerformanceQueue.add(studentVM);
    }
  }

  /**
   * Konverterer en model Laptop til en LaptopViewModel.
   */
  private LaptopViewModel convertToLaptopViewModel(Laptop laptop) {
    String id = laptop.getId().toString();
    String brand = laptop.getBrand();
    String model = laptop.getModel();
    int ram = laptop.getRam();
    int diskSize = laptop.getGigabyte();
    String performanceType = laptop.getPerformanceType().toString();
    String status = laptop.getStateClassName();

    // Find studenten der bruger denne laptop (hvis udlånt)
    String assignedTo = "";
    if (!laptop.isAvailable()) {
      List<Reservation> activeReservations = this.model.getAllActiveReservations();
      for (Reservation reservation : activeReservations) {
        if (reservation.getLaptop().getId().equals(laptop.getId())) {
          assignedTo = reservation.getStudent().getName();
          break;
        }
      }
    }

    return new LaptopViewModel(id, brand, model, ram, diskSize, performanceType, status, assignedTo);
  }

  /**
   * Konverterer en model Student til en StudentViewModel.
   */
  private StudentViewModel convertToStudentViewModel(Student student) {
    String viaId = String.valueOf(student.getViaId());
    String name = student.getName();
    String email = student.getEmail();
    String phoneNumber = String.valueOf(student.getPhoneNumber());
    String degreeTitle = student.getDegreeTitle();

    // Formatér dato
    Date endDate = student.getDegreeEndDate();
    String degreeEndDate = endDate != null ? endDate.toString() : "";

    String performanceNeeded = student.getPerformanceNeeded().toString();
    boolean hasLaptop = student.isHasLaptop();

    StudentViewModel studentVM = new StudentViewModel(viaId, name, email, phoneNumber,
            degreeTitle, degreeEndDate, performanceNeeded, hasLaptop);

    // Sæt kø dato (simpel timestamp for nu)
    studentVM.setQueueDate(new Date().toString());

    return studentVM;
  }

  /**
   * Opretter en ny student og tildeler laptop hvis muligt.
   *
   * @param isHighPerformance Om studenten har behov for høj-ydelses laptop
   * @param endDate Uddannelsesslutdato
   */
  public void createStudent(boolean isHighPerformance, LocalDate endDate) {
    // Valider input og vis fejlbesked hvis nødvendigt
    if (!validateInput(endDate)) {
      return;
    }

    try {
      // Konverter input
      String studentName = name.get();
      Date degreeEnd = java.sql.Date.valueOf(endDate);
      String title = degreeTitle.get();
      int studentViaId = Integer.parseInt(viaId.get());
      String studentEmail = email.get();
      int studentPhone = Integer.parseInt(phoneNumber.get());
      PerformanceTypeEnum performanceNeeded = isHighPerformance ?
              PerformanceTypeEnum.HIGH : PerformanceTypeEnum.LOW;

      // Opret student gennem model
      Student student = model.createStudent(studentName, degreeEnd, title,
              studentViaId, studentEmail, studentPhone, performanceNeeded);

      if (student != null) {
        // Opdater resultat info
        resultStudent.set(student.getName() + " (VIA ID: " + student.getViaId() + ")");

        // Tjek om studenten fik tildelt en laptop
        if (student.isHasLaptop()) {
          // Find reservation for at få laptop info
          Reservation reservation = findReservationForStudent(student.getViaId());
          if (reservation != null) {
            Laptop assignedLaptop = reservation.getLaptop();
            resultLaptop.set(assignedLaptop.getBrand() + " " + assignedLaptop.getModel());
            resultStatus.set("Laptop tildelt");
            statusMessage.set("Student oprettet og laptop tildelt");
          } else {
            resultLaptop.set("Ukendt laptop");
            resultStatus.set("Laptop tildelt, men detaljer ukendte");
          }
        } else {
          resultLaptop.set("Ingen tilgængelig laptop");
          resultStatus.set("Student sat i " + (isHighPerformance ? "høj" : "lav") + "-ydelses kø");
          statusMessage.set("Student oprettet og tilføjet til venteliste");
        }

        // Refresh data for at vise opdateret info
        refreshData();

        // Ryd formularfelter
        clearFormFields();

        // Ryd fejlbesked
        errorMessage.set("");
      } else {
        errorMessage.set("Kunne ikke oprette student - muligvis findes VIA ID allerede");
        statusMessage.set("Fejl ved oprettelse af student");
      }
    } catch (NumberFormatException e) {
      errorMessage.set("Ugyldig input: VIA ID og telefonnummer skal være tal");
      statusMessage.set("Fejl ved konvertering af numeriske værdier");
    } catch (Exception e) {
      errorMessage.set("En fejl opstod: " + e.getMessage());
      statusMessage.set("Uventet fejl ved oprettelse af student");
    }
  }

  /**
   * Finder en reservation for en specifik student.
   */
  private Reservation findReservationForStudent(int viaId) {
    List<Reservation> activeReservations = model.getAllActiveReservations();
    for (Reservation reservation : activeReservations) {
      if (reservation.getStudent().getViaId() == viaId) {
        return reservation;
      }
    }
    return null;
  }

  /**
   * Validerer formularinput.
   */
  private boolean validateInput(LocalDate endDate) {
    // Ryd tidligere fejlbesked
    errorMessage.set("");

    // Tjek om alle felter er udfyldt
    if (name.get() == null || name.get().trim().isEmpty()) {
      errorMessage.set("Navn skal udfyldes");
      return false;
    }

    if (email.get() == null || email.get().trim().isEmpty()) {
      errorMessage.set("Email skal udfyldes");
      return false;
    }

    if (viaId.get() == null || viaId.get().trim().isEmpty()) {
      errorMessage.set("VIA ID skal udfyldes");
      return false;
    }

    if (phoneNumber.get() == null || phoneNumber.get().trim().isEmpty()) {
      errorMessage.set("Telefonnummer skal udfyldes");
      return false;
    }

    if (degreeTitle.get() == null || degreeTitle.get().trim().isEmpty()) {
      errorMessage.set("Uddannelsestitel skal udfyldes");
      return false;
    }

    if (endDate == null) {
      errorMessage.set("Uddannelse slutdato skal vælges");
      return false;
    }

    // Valider VIA ID format
    try {
      int id = Integer.parseInt(viaId.get());
      if (id <= 0) {
        errorMessage.set("VIA ID skal være et positivt tal");
        return false;
      }
    } catch (NumberFormatException e) {
      errorMessage.set("VIA ID skal være et tal");
      return false;
    }

    // Valider telefonnummer format
    try {
      int phone = Integer.parseInt(phoneNumber.get());
      if (phone <= 0) {
        errorMessage.set("Telefonnummer skal være et positivt tal");
        return false;
      }
    } catch (NumberFormatException e) {
      errorMessage.set("Telefonnummer skal være et tal");
      return false;
    }

    // Valider email format (simpel tjek)
    if (!email.get().contains("@")) {
      errorMessage.set("Email skal indeholde @");
      return false;
    }

    // Tjek at uddannelse slutdato er i fremtiden
    if (endDate.isBefore(LocalDate.now())) {
      errorMessage.set("Uddannelse slutdato skal være i fremtiden");
      return false;
    }

    return true;
  }

  /**
   * Rydder alle formularfelter.
   */
  private void clearFormFields() {
    name.set("");
    email.set("");
    viaId.set("");
    phoneNumber.set("");
    degreeTitle.set("");
    degreeEndDate.set(null);
    lowPerformanceSelected.set(true);
  }

  /**
   * Sætter uddannelsesslutdato.
   */
  public void setDegreeEndDate(LocalDate date) {
    degreeEndDate.set(date);
  }

  /**
   * Håndterer propertyChange events fra model.
   */
  @Override
  public void propertyChange(PropertyChangeEvent evt) {
    // Forskellige events skal håndteres forskelligt
    String propertyName = evt.getPropertyName();

    // Laptop-relaterede events
    if ("laptopStateChanged".equals(propertyName) ||
            "laptopCreated".equals(propertyName) ||
            "laptopUpdated".equals(propertyName) ||
            "laptopRemoved".equals(propertyName)) {

      refreshLaptops();
    }

    // Kø-relaterede events
    if ("highQueueSize".equals(propertyName) ||
            "lowQueueSize".equals(propertyName) ||
            "studentAdded".equals(propertyName) ||
            "studentRemoved".equals(propertyName)) {

      refreshQueues();
    }

    // Reservations-relaterede events
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

  public StringProperty nameProperty() {
    return name;
  }

  public StringProperty emailProperty() {
    return email;
  }

  public StringProperty viaIdProperty() {
    return viaId;
  }

  public StringProperty phoneNumberProperty() {
    return phoneNumber;
  }

  public StringProperty degreeTitleProperty() {
    return degreeTitle;
  }

  public ObjectProperty<LocalDate> degreeEndDateProperty() {
    return degreeEndDate;
  }

  public BooleanProperty lowPerformanceSelectedProperty() {
    return lowPerformanceSelected;
  }

  public StringProperty errorMessageProperty() {
    return errorMessage;
  }

  public StringProperty resultStudentProperty() {
    return resultStudent;
  }

  public StringProperty resultStatusProperty() {
    return resultStatus;
  }

  public StringProperty resultLaptopProperty() {
    return resultLaptop;
  }

  public StringProperty statusMessageProperty() {
    return statusMessage;
  }

  public ObservableList<LaptopViewModel> getHighPerformanceLaptops() {
    return highPerformanceLaptops;
  }

  public ObservableList<LaptopViewModel> getLowPerformanceLaptops() {
    return lowPerformanceLaptops;
  }

  public ObservableList<StudentViewModel> getHighPerformanceQueue() {
    return highPerformanceQueue;
  }

  public ObservableList<StudentViewModel> getLowPerformanceQueue() {
    return lowPerformanceQueue;
  }
}