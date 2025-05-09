package view.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import view.ViewHandler;
import viewmodel.AvailableLaptopsViewModel;
import viewmodel.ViewModelFactory;
import viewmodel.model.LaptopViewModel;

/**
 * Controller for visning af tilgængelige laptops.
 * Viser opdelt liste over højydelses- og lavydelses-computere.
 */
public class AvailableLaptopsViewController implements Controller {

  // UI components - Statistik
  @FXML
  private Label totalAvailableCountLabel;
  @FXML
  private Label highPerformanceCountLabel;
  @FXML
  private Label lowPerformanceCountLabel;

  // UI components - Høj-ydelses computere tabel
  @FXML
  private TableView<LaptopViewModel> highPerformanceTable;
  @FXML
  private TableColumn<LaptopViewModel, String> highIdColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> highBrandColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> highModelColumn;
  @FXML
  private TableColumn<LaptopViewModel, Number> highRamColumn;
  @FXML
  private TableColumn<LaptopViewModel, Number> highDiskColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> highPerformanceColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> highStatusColumn;

  // UI components - Lav-ydelses computere tabel
  @FXML
  private TableView<LaptopViewModel> lowPerformanceTable;
  @FXML
  private TableColumn<LaptopViewModel, String> lowIdColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> lowBrandColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> lowModelColumn;
  @FXML
  private TableColumn<LaptopViewModel, Number> lowRamColumn;
  @FXML
  private TableColumn<LaptopViewModel, Number> lowDiskColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> lowPerformanceColumn;
  @FXML
  private TableColumn<LaptopViewModel, String> lowStatusColumn;

  // UI components - Knapper
  @FXML
  private Button refreshButton;
  @FXML
  private Button backButton;
  @FXML
  private Button exitButton;

  // References to handler and viewmodel
  private ViewHandler viewHandler;
  private AvailableLaptopsViewModel viewModel;

  /**
   * Initialiserer controlleren med nødvendige referencer.
   *
   * @param viewHandler Reference til ViewHandler for navigering mellem views
   * @param viewModelFactory Factory som giver adgang til ViewModels
   */
  @Override
  public void init(ViewHandler viewHandler, ViewModelFactory viewModelFactory) {
    this.viewHandler = viewHandler;
    this.viewModel = viewModelFactory.getAvailableLaptopsViewModel();
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
    totalAvailableCountLabel.textProperty().bind(viewModel.totalAvailableCountProperty().asString());
    highPerformanceCountLabel.textProperty().bind(viewModel.highPerformanceCountProperty().asString());
    lowPerformanceCountLabel.textProperty().bind(viewModel.lowPerformanceCountProperty().asString());
  }

  /**
   * Opsætter tabeller og deres kolonner med data fra ViewModel.
   */
  private void setupTables() {
    // Opsæt højydelses tabel
    highPerformanceTable.setItems(viewModel.getHighPerformanceLaptops());

    highIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
    highBrandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());
    highModelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
    highRamColumn.setCellValueFactory(cellData -> cellData.getValue().ramProperty());
    highDiskColumn.setCellValueFactory(cellData -> cellData.getValue().diskSizeProperty());
    highPerformanceColumn.setCellValueFactory(cellData -> cellData.getValue().performanceTypeProperty());
    highStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());

    // Opsæt lavydelses tabel
    lowPerformanceTable.setItems(viewModel.getLowPerformanceLaptops());

    lowIdColumn.setCellValueFactory(cellData -> cellData.getValue().idProperty());
    lowBrandColumn.setCellValueFactory(cellData -> cellData.getValue().brandProperty());
    lowModelColumn.setCellValueFactory(cellData -> cellData.getValue().modelProperty());
    lowRamColumn.setCellValueFactory(cellData -> cellData.getValue().ramProperty());
    lowDiskColumn.setCellValueFactory(cellData -> cellData.getValue().diskSizeProperty());
    lowPerformanceColumn.setCellValueFactory(cellData -> cellData.getValue().performanceTypeProperty());
    lowStatusColumn.setCellValueFactory(cellData -> cellData.getValue().statusProperty());
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