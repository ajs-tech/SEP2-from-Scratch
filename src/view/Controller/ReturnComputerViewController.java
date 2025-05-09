package view.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import view.ViewHandler;
import viewmodel.ReturnComputerViewModel;
import viewmodel.ViewModelFactory;
import viewmodel.model.StudentLoanViewModel;

/**
 * Controller for håndtering af laptop returnering.
 * Muliggør søgning efter studerende med udlånt udstyr og returnering af laptops.
 */
public class ReturnComputerViewController implements Controller {

  // Søgefelt og type
  @FXML
  private ComboBox<String> searchTypeComboBox;
  @FXML
  private TextField searchField;
  @FXML
  private Button searchButton;
  @FXML
  private Button clearSearchButton;
  @FXML
  private Label searchErrorLabel;

  // Studerende/laptops tabel
  @FXML
  private TableView<StudentLoanViewModel> studentLaptopTable;
  @FXML
  private TableColumn<StudentLoanViewModel, String> viaIdColumn;
  @FXML
  private TableColumn<StudentLoanViewModel, String> studentNameColumn;
  @FXML
  private TableColumn<StudentLoanViewModel, String> emailColumn;
  @FXML
  private TableColumn<StudentLoanViewModel, String> phoneColumn;
  @FXML
  private TableColumn<StudentLoanViewModel, String> laptopBrandColumn;
  @FXML
  private TableColumn<StudentLoanViewModel, String> laptopModelColumn;
  @FXML
  private TableColumn<StudentLoanViewModel, String> laptopSpecsColumn;
  @FXML
  private TableColumn<StudentLoanViewModel, String> loanDateColumn;
  @FXML
  private Button refreshButton;
  @FXML
  private Button returnButton;

  // Returneringsoplysninger
  @FXML
  private VBox returnResultPanel;
  @FXML
  private Label resultStudentLabel;
  @FXML
  private Label resultComputerLabel;
  @FXML
  private Label resultDateLabel;
  @FXML
  private Label resultStatusLabel;

  // Andre UI komponenter
  @FXML
  private Label statusLabel;
  @FXML
  private Button backButton;
  @FXML
  private Button exitButton;

  // References to handler and viewmodel
  private ViewHandler viewHandler;
  private ReturnComputerViewModel viewModel;

  /**
   * Initialiserer controlleren med nødvendige referencer.
   *
   * @param viewHandler Reference til ViewHandler for navigering mellem views
   * @param viewModelFactory Factory som giver adgang til ViewModels
   */
  @Override
  public void init(ViewHandler viewHandler, ViewModelFactory viewModelFactory) {
    this.viewHandler = viewHandler;
    this.viewModel = viewModelFactory.getReturnComputerViewModel();
    setupBindings();
    setupTable();
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
    // Bind search fields
    searchField.textProperty().bindBidirectional(viewModel.searchTermProperty());

    // Bind error labels
    searchErrorLabel.textProperty().bind(viewModel.errorMessageProperty());

    // Bind result labels
    resultStudentLabel.textProperty().bind(viewModel.resultStudentProperty());
    resultComputerLabel.textProperty().bind(viewModel.resultComputerProperty());
    resultDateLabel.textProperty().bind(viewModel.resultDateProperty());
    resultStatusLabel.textProperty().bind(viewModel.resultStatusProperty());

    // Bind return button disable state
    returnButton.disableProperty().bind(viewModel.returnButtonDisabledProperty());

    // Bind status label
    statusLabel.textProperty().bind(viewModel.statusMessageProperty());
  }

  /**
   * Opsætter tabel kolonner med data binding.
   */
  private void setupTable() {
    // Set items for student/laptop table
    studentLaptopTable.setItems(viewModel.getStudentLoans());

    // Configure columns
    viaIdColumn.setCellValueFactory(cellData -> cellData.getValue().viaIdProperty());
    studentNameColumn.setCellValueFactory(cellData -> cellData.getValue().studentNameProperty());
    emailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
    phoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());
    laptopBrandColumn.setCellValueFactory(cellData -> cellData.getValue().laptopBrandProperty());
    laptopModelColumn.setCellValueFactory(cellData -> cellData.getValue().laptopModelProperty());
    laptopSpecsColumn.setCellValueFactory(cellData -> cellData.getValue().laptopSpecsProperty());
    loanDateColumn.setCellValueFactory(cellData -> cellData.getValue().loanDateProperty());
  }

  /**
   * Opsætter event listeners.
   */
  private void setupListeners() {
    // Search type combo box listener
    searchTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null) {
        viewModel.setSearchType(newVal);
      }
    });

    // Select first search type by default
    if (!searchTypeComboBox.getItems().isEmpty()) {
      searchTypeComboBox.getSelectionModel().selectFirst();
    }

    // Table selection listener
    studentLaptopTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      viewModel.setSelectedLoan(newVal);
    });
  }

  /**
   * Håndterer klik på "Søg" knappen.
   */
  @FXML
  private void onSearchStudent() {
    String searchType = searchTypeComboBox.getSelectionModel().getSelectedItem();
    viewModel.searchStudents(searchType);
  }

  /**
   * Håndterer klik på "Ryd" knappen for søgefeltet.
   */
  @FXML
  private void onClearSearch() {
    clearFields();
    viewModel.clearSearch();
  }

  /**
   * Håndterer klik på "Opdater liste" knappen.
   */
  @FXML
  private void onRefreshList() {
    viewModel.refreshData();
  }

  /**
   * Håndterer klik på "Returner valgt computer" knappen.
   */
  @FXML
  private void onReturnComputer() {
    viewModel.returnSelectedLaptop();
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
    searchField.clear();
    searchTypeComboBox.getSelectionModel().selectFirst();
  }
}