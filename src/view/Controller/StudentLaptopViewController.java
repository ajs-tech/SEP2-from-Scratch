package view.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import core.ViewHandler;
import viewmodel.StudentLaptopViewModel;
import viewmodel.ViewModelFactory;
import viewmodel.model.LaptopViewModel;
import viewmodel.model.StudentViewModel;

import java.time.LocalDate;

/**
 * Controller for student registrering og laptop tildeling.
 * Håndterer oprettelse af nye studerende, automatisk tildeling af laptops,
 * og visning af ventelister.
 */
public class StudentLaptopViewController implements Controller {

  // Student registreringsformular
  @FXML
  private TextField nameField;
  @FXML
  private TextField emailField;
  @FXML
  private TextField viaIdField;
  @FXML
  private TextField phoneNumberField;
  @FXML
  private TextField degreeTitleField;
  @FXML
  private DatePicker degreeEndDatePicker;
  @FXML
  private RadioButton lowPerformanceRadio;
  @FXML
  private RadioButton highPerformanceRadio;
  @FXML
  private ToggleGroup performanceGroup;
  @FXML
  private Button createStudentButton;
  @FXML
  private Button clearStudentButton;
  @FXML
  private Label studentErrorLabel;

  // Resultat panel
  @FXML
  private VBox assignmentResultPanel;
  @FXML
  private Label resultStudentLabel;
  @FXML
  private Label resultStatusLabel;
  @FXML
  private Label resultLaptopLabel;

  // Laptop tabeller
  @FXML
  private TableView<LaptopViewModel> highPerformanceLaptopsTable;
  @FXML
  private TableColumn<LaptopViewModel, String> highLaptopBrandColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> highLaptopModelColumn;
  @FXML
  private TableColumn<LaptopViewModel, Number> highLaptopRamColumn;
  @FXML
  private TableColumn<LaptopViewModel, Number> highLaptopDiskColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> highLaptopStatusColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> highLaptopStudentColumn;

  @FXML
  private TableView<LaptopViewModel> lowPerformanceLaptopsTable;
  @FXML
  private TableColumn<LaptopViewModel, String> lowLaptopBrandColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> lowLaptopModelColumn;
  @FXML
  private TableColumn<LaptopViewModel, Number> lowLaptopRamColumn;
  @FXML
  private TableColumn<LaptopViewModel, Number> lowLaptopDiskColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> lowLaptopStatusColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> lowLaptopStudentColumn;

  // Venteliste tabeller
  @FXML
  private TableView<StudentViewModel> highPerformanceQueueTable;
  @FXML
  private TableColumn<StudentViewModel, String> highQueueViaIdColumn;
  @FXML
  private TableColumn<StudentViewModel, String> highQueueNameColumn;
  @FXML
  private TableColumn<StudentViewModel, String> highQueueEmailColumn;
  @FXML
  private TableColumn<StudentViewModel, String> highQueuePhoneColumn;
  @FXML
  private TableColumn<StudentViewModel, String> highQueueDateColumn;

  @FXML
  private TableView<StudentViewModel> lowPerformanceQueueTable;
  @FXML
  private TableColumn<StudentViewModel, String> lowQueueViaIdColumn;
  @FXML
  private TableColumn<StudentViewModel, String> lowQueueNameColumn;
  @FXML
  private TableColumn<StudentViewModel, String> lowQueueEmailColumn;
  @FXML
  private TableColumn<StudentViewModel, String> lowQueuePhoneColumn;
  @FXML
  private TableColumn<StudentViewModel, String> lowQueueDateColumn;

  // Andre UI komponenter
  @FXML
  private Button refreshLaptopsButton;
  @FXML
  private Button refreshQueuesButton;
  @FXML
  private Label statusLabel;
  @FXML
  private Button backButton;
  @FXML
  private Button exitButton;

  // References to handler and viewmodel
  private ViewHandler viewHandler;
  private StudentLaptopViewModel viewModel;

  /**
   * Initialiserer controlleren med nødvendige referencer.
   *
   * @param viewHandler Reference til ViewHandler for navigering mellem views
   * @param viewModelFactory Factory som giver adgang til ViewModels
   */
  @Override
  public void init(ViewHandler viewHandler, ViewModelFactory viewModelFactory) {
    this.viewHandler = viewHandler;
    this.viewModel = viewModelFactory.getStudentLaptopViewModel();
    setupBindings();
    setupTables();
    setupListeners();
  }

  /**
   * Genindlæser viewet med opdaterede data.
   */
  @Override
  public void reset() {
    viewModel.refreshData();
    clearFields();
  }

  /**
   * Opsætter binding mellem UI-elementer og ViewModel.
   */
  private void setupBindings() {
    // Bind form fields til ViewModel properties
    nameField.textProperty().bindBidirectional(viewModel.nameProperty());
    emailField.textProperty().bindBidirectional(viewModel.emailProperty());
    viaIdField.textProperty().bindBidirectional(viewModel.viaIdProperty());
    phoneNumberField.textProperty().bindBidirectional(viewModel.phoneNumberProperty());
    degreeTitleField.textProperty().bindBidirectional(viewModel.degreeTitleProperty());

    // Bind radiobuttons
    lowPerformanceRadio.selectedProperty().bindBidirectional(viewModel.lowPerformanceSelectedProperty());

    // Bind error label
    studentErrorLabel.textProperty().bind(viewModel.errorMessageProperty());

    // Bind result labels
    resultStudentLabel.textProperty().bind(viewModel.resultStudentProperty());
    resultStatusLabel.textProperty().bind(viewModel.resultStatusProperty());
    resultLaptopLabel.textProperty().bind(viewModel.resultLaptopProperty());

    // Bind status label
    statusLabel.textProperty().bind(viewModel.statusMessageProperty());
  }

  /**
   * Opsætter tabel kolonner med data binding.
   */
  private void setupTables() {
    // Set items for laptop tables
    highPerformanceLaptopsTable.setItems(viewModel.getHighPerformanceLaptops());
    lowPerformanceLaptopsTable.setItems(viewModel.getLowPerformanceLaptops());

    // Configure high performance laptop columns
    highLaptopBrandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());
    highLaptopModelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
    highLaptopRamColumn.setCellValueFactory(cellData -> cellData.getValue().ramProperty());
    highLaptopDiskColumn.setCellValueFactory(cellData -> cellData.getValue().diskSizeProperty());
    highLaptopStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    highLaptopStudentColumn.setCellValueFactory(cellData -> cellData.getValue().assignedToProperty());

    // Configure low performance laptop columns
    lowLaptopBrandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());
    lowLaptopModelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
    lowLaptopRamColumn.setCellValueFactory(cellData -> cellData.getValue().ramProperty());
    lowLaptopDiskColumn.setCellValueFactory(cellData -> cellData.getValue().diskSizeProperty());
    lowLaptopStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
    lowLaptopStudentColumn.setCellValueFactory(cellData -> cellData.getValue().assignedToProperty());

    // Set items for queue tables
    highPerformanceQueueTable.setItems(viewModel.getHighPerformanceQueue());
    lowPerformanceQueueTable.setItems(viewModel.getLowPerformanceQueue());

    // Configure high performance queue columns
    highQueueViaIdColumn.setCellValueFactory(cellData -> cellData.getValue().viaIdProperty());
    highQueueNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
    highQueueEmailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
    highQueuePhoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());
    highQueueDateColumn.setCellValueFactory(cellData -> cellData.getValue().queueDateProperty());

    // Configure low performance queue columns
    lowQueueViaIdColumn.setCellValueFactory(cellData -> cellData.getValue().viaIdProperty());
    lowQueueNameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
    lowQueueEmailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
    lowQueuePhoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());
    lowQueueDateColumn.setCellValueFactory(cellData -> cellData.getValue().queueDateProperty());
  }

  /**
   * Opsætter event listeners.
   */
  private void setupListeners() {
    // DatePicker listener
    degreeEndDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null) {
        viewModel.setDegreeEndDate(newVal);
      }
    });
  }

  /**
   * Håndterer klik på "Opret studerende" knappen.
   */
  @FXML
  private void onCreateStudent() {
    LocalDate endDate = degreeEndDatePicker.getValue();
    boolean isHighPerformance = highPerformanceRadio.isSelected();

    viewModel.createStudent(isHighPerformance, endDate);
  }

  /**
   * Håndterer klik på "Ryd formular" knappen.
   */
  @FXML
  private void onClearStudentForm() {
    clearFields();
  }

  /**
   * Håndterer klik på "Opdater liste" knappen for laptops.
   */
  @FXML
  private void onRefreshLaptops() {
    viewModel.refreshLaptops();
  }

  /**
   * Håndterer klik på "Opdater ventelister" knappen.
   */
  @FXML
  private void onRefreshQueues() {
    viewModel.refreshQueues();
  }

  /**
   * Håndterer klik på "Tilbage til menu" knappen.
   */
  @FXML
  private void onBack() {
    viewHandler.openMainMenu();
  }

  /**
   * Håndterer klik på "Afslut" knappen.
   */
  @FXML
  private void onExit() {
    viewModel.closeApplication();
    viewHandler.closeApplication();
  }

  /**
   * Rydder alle formularfelter.
   */
  private void clearFields() {
    nameField.clear();
    emailField.clear();
    viaIdField.clear();
    phoneNumberField.clear();
    degreeTitleField.clear();
    degreeEndDatePicker.setValue(null);
    lowPerformanceRadio.setSelected(true);
  }
}