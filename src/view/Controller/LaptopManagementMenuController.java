package view.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import view.ViewHandler;
import viewmodel.LaptopManagementMenuViewModel;
import viewmodel.ViewModelFactory;

/**
 * Controller for hovedmenuen i Laptop Udlånssystemet.
 * Håndterer navigation til andre views og viser overordnet systemstatus.
 */
public class LaptopManagementMenuController implements Controller {

  // UI components
  @FXML
  private Label statusLabel;

  @FXML
  private Label availableLabel;

  @FXML
  private Label loanedLabel;

  @FXML
  private Button loanButton;

  @FXML
  private Button returnButton;

  @FXML
  private Button overviewButton;

  @FXML
  private Button availableButton;

  @FXML
  private Button exitButton;

  // References to handler and viewmodel
  private ViewHandler viewHandler;
  private LaptopManagementMenuViewModel viewModel;

  /**
   * Initialiserer controlleren med nødvendige referencer.
   *
   * @param viewHandler Reference til ViewHandler for navigering mellem views
   * @param viewModelFactory Factory som giver adgang til ViewModels
   */
  @Override
  public void init(ViewHandler viewHandler, ViewModelFactory viewModelFactory) {
    this.viewHandler = viewHandler;
    this.viewModel = viewModelFactory.getLaptopManagementMenuViewModel();
    bindToViewModel();
  }

  /**
   * Genindlæser viewet med opdaterede data.
   */
  @Override
  public void reset() {
    viewModel.refreshData();
  }

  /**
   * Binder UI-elementer til ViewModel properties.
   */
  private void bindToViewModel() {
    // Bind labels to property values from ViewModel
    statusLabel.textProperty().bind(viewModel.systemStatusProperty());
    availableLabel.textProperty().bind(viewModel.availableLaptopsTextProperty());
    loanedLabel.textProperty().bind(viewModel.loanedLaptopsTextProperty());

    // Button disable bindings could be added here if needed
    // loanButton.disableProperty().bind(viewModel.loanButtonDisabledProperty());
  }

  /**
   * Håndterer klik på "Udlån Computer" knappen.
   */
  @FXML
  private void handleLoanLaptop() {
    viewHandler.openStudentLaptopView();
  }

  /**
   * Håndterer klik på "Returner Computer" knappen.
   */
  @FXML
  private void handleReturnLaptop() {
    viewHandler.openReturnComputerView();
  }

  /**
   * Håndterer klik på "Se Låneoversigt" knappen.
   */
  @FXML
  private void handleLoanOverview() {
    viewHandler.openLoanOverview();
  }

  /**
   * Håndterer klik på "Tilgængelige Computere" knappen.
   */
  @FXML
  private void handleAvailableLaptops() {
    viewHandler.openAvailableLaptops();
  }

  /**
   * Håndterer klik på "Afslut Program" knappen.
   */
  @FXML
  private void handleExit() {
    viewModel.closeApplication();
    viewHandler.closeApplication();
  }
}