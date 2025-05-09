package view.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import core.ViewHandler;
import viewmodel.LoanOverviewViewModel;
import viewmodel.ViewModelFactory;
import viewmodel.LoanViewModel;

/**
 * Controller for visning af låneoversigt.
 * Viser aktive lån opdelt efter computertype (høj/lav-ydelses).
 */
public class LoanOverviewViewController implements Controller {

  // UI components - Statistik
  @FXML
  private Label activeLoansCountLabel;
  @FXML
  private Label highPerformanceCountLabel;
  @FXML
  private Label lowPerformanceCountLabel;

  // UI components - Høj-ydelses lån tabel
  @FXML
  private TableView<LoanViewModel> highPerformanceTable;
  @FXML
  private TableColumn<LoanViewModel, String> highStudentNameColumn;
  @FXML
  private TableColumn<LoanViewModel, String> highViaIdColumn;
  @FXML
  private TableColumn<LoanViewModel, String> highEmailColumn;
  @FXML
  private TableColumn<LoanViewModel, String> highPhoneColumn;
  @FXML
  private TableColumn<LoanViewModel, String> highLaptopBrandColumn;
  @FXML
  private TableColumn<LoanViewModel, String> highLaptopModelColumn;
  @FXML
  private TableColumn<LoanViewModel, String> highSpecsColumn;
  @FXML
  private TableColumn<LoanViewModel, String> highLoanDateColumn;

  // UI components - Lav-ydelses lån tabel
  @FXML
  private TableView<LoanViewModel> lowPerformanceTable;
  @FXML
  private TableColumn<LoanViewModel, String> lowStudentNameColumn;
  @FXML
  private TableColumn<LoanViewModel, String> lowViaIdColumn;
  @FXML
  private TableColumn<LoanViewModel, String> lowEmailColumn;
  @FXML
  private TableColumn<LoanViewModel, String> lowPhoneColumn;
  @FXML
  private TableColumn<LoanViewModel, String> lowLaptopBrandColumn;
  @FXML
  private TableColumn<LoanViewModel, String> lowLaptopModelColumn;
  @FXML
  private TableColumn<LoanViewModel, String> lowSpecsColumn;
  @FXML
  private TableColumn<LoanViewModel, String> lowLoanDateColumn;

  // UI components - Knapper
  @FXML
  private Button refreshButton;
  @FXML
  private Button backButton;
  @FXML
  private Button exitButton;

  // References to handler and viewmodel
  private ViewHandler viewHandler;
  private LoanOverviewViewModel viewModel;

  /**
   * Initialiserer controlleren med nødvendige referencer.
   *
   * @param viewHandler Reference til ViewHandler for navigering mellem views
   * @param viewModelFactory Factory som giver adgang til ViewModels
   */
  @Override
  public void init(ViewHandler viewHandler, ViewModelFactory viewModelFactory) {
    this.viewHandler = viewHandler;
    this.viewModel = viewModelFactory.getLoanOverviewViewModel();
    setupBindings();
    setupTables();
  }

  /**
   * Genindlæser viewet med opdaterede data.
   */
  @Override
  public void reset() {
    viewModel.refreshData();
  }

  /**
   * Opsætter binding mellem UI-elementer og ViewModel.
   */
  private void setupBindings() {
    // Bind labels til properties fra ViewModel
    activeLoansCountLabel.textProperty().bind(viewModel.totalActiveLoansProperty().asString());
    highPerformanceCountLabel.textProperty().bind(viewModel.highPerformanceLoansProperty().asString());
    lowPerformanceCountLabel.textProperty().bind(viewModel.lowPerformanceLoansProperty().asString());
  }

  /**
   * Opsætter tabeller og deres kolonner med data fra ViewModel.
   */
  private void setupTables() {
    // Opsæt højydelses lån tabel
    highPerformanceTable.setItems(viewModel.getHighPerformanceLoans());

    highStudentNameColumn.setCellValueFactory(cellData -> cellData.getValue().studentNameProperty());
    highViaIdColumn.setCellValueFactory(cellData -> cellData.getValue().viaIdProperty());
    highEmailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
    highPhoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());
    highLaptopBrandColumn.setCellValueFactory(cellData -> cellData.getValue().laptopBrandProperty());
    highLaptopModelColumn.setCellValueFactory(cellData -> cellData.getValue().laptopModelProperty());
    highSpecsColumn.setCellValueFactory(cellData -> cellData.getValue().laptopSpecsProperty());
    highLoanDateColumn.setCellValueFactory(cellData -> cellData.getValue().loanDateProperty());

    // Opsæt lavydelses lån tabel
    lowPerformanceTable.setItems(viewModel.getLowPerformanceLoans());

    lowStudentNameColumn.setCellValueFactory(cellData -> cellData.getValue().studentNameProperty());
    lowViaIdColumn.setCellValueFactory(cellData -> cellData.getValue().viaIdProperty());
    lowEmailColumn.setCellValueFactory(cellData -> cellData.getValue().emailProperty());
    lowPhoneColumn.setCellValueFactory(cellData -> cellData.getValue().phoneNumberProperty());
    lowLaptopBrandColumn.setCellValueFactory(cellData -> cellData.getValue().laptopBrandProperty());
    lowLaptopModelColumn.setCellValueFactory(cellData -> cellData.getValue().laptopModelProperty());
    lowSpecsColumn.setCellValueFactory(cellData -> cellData.getValue().laptopSpecsProperty());
    lowLoanDateColumn.setCellValueFactory(cellData -> cellData.getValue().loanDateProperty());
  }

  /**
   * Håndterer klik på "Opdater oversigt" knappen.
   */
  @FXML
  private void onRefresh() {
    viewModel.refreshData();
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
}