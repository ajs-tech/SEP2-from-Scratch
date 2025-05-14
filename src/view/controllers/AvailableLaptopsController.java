package view.controllers;

import core.ViewHandler;
import core.ViewmModelFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import viewmodel.AvailableLaptopsViewModel;
import viewmodel.AvailableLaptopsViewModel.LaptopTableItem;

public class AvailableLaptopsController implements ViewController {

    private ViewHandler viewHandler;
    private AvailableLaptopsViewModel viewModel;

    // Statistik labels
    @FXML private Label totalAvailableCountLabel;
    @FXML private Label highPerformanceCountLabel;
    @FXML private Label lowPerformanceCountLabel;
    @FXML private Button createLaptopButton;

    // TableView for høj-ydelses computere
    @FXML private TableView<LaptopTableItem> highPerformanceTable;
    @FXML private TableColumn<LaptopTableItem, String> highIdColumn;
    @FXML private TableColumn<LaptopTableItem, String> highBrandColumn;
    @FXML private TableColumn<LaptopTableItem, String> highModelColumn;
    @FXML private TableColumn<LaptopTableItem, Integer> highRamColumn;
    @FXML private TableColumn<LaptopTableItem, Integer> highDiskColumn;
    @FXML private TableColumn<LaptopTableItem, String> highPerformanceColumn;
    @FXML private TableColumn<LaptopTableItem, String> highStatusColumn;

    // TableView for lav-ydelses computere
    @FXML private TableView<LaptopTableItem> lowPerformanceTable;
    @FXML private TableColumn<LaptopTableItem, String> lowIdColumn;
    @FXML private TableColumn<LaptopTableItem, String> lowBrandColumn;
    @FXML private TableColumn<LaptopTableItem, String> lowModelColumn;
    @FXML private TableColumn<LaptopTableItem, Integer> lowRamColumn;
    @FXML private TableColumn<LaptopTableItem, Integer> lowDiskColumn;
    @FXML private TableColumn<LaptopTableItem, String> lowPerformanceColumn;
    @FXML private TableColumn<LaptopTableItem, String> lowStatusColumn;

    // Knapper
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button exitButton;

    @Override
    public void init(ViewHandler viewHandler, ViewmModelFactory viewModelFactory) {
        this.viewHandler = viewHandler;
        this.viewModel = viewModelFactory.getAvailableLaptopsViewModel();

        setupBindings();
        setupTables();
    }

    private void setupBindings() {
        // Bind statistics labels
        totalAvailableCountLabel.textProperty().bind(viewModel.totalAvailableCountProperty().asString("%d"));
        highPerformanceCountLabel.textProperty().bind(viewModel.highPerformanceCountProperty().asString("%d"));
        lowPerformanceCountLabel.textProperty().bind(viewModel.lowPerformanceCountProperty().asString("%d"));
    }

    private void setupTables() {
        // Setup high-performance table
        highIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        highBrandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        highModelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        highRamColumn.setCellValueFactory(new PropertyValueFactory<>("ram"));
        highDiskColumn.setCellValueFactory(new PropertyValueFactory<>("disk"));
        highPerformanceColumn.setCellValueFactory(new PropertyValueFactory<>("performanceType"));
        highStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        highPerformanceTable.setItems(viewModel.getHighPerformanceLaptops());

        // Setup low-performance table
        lowIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        lowBrandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        lowModelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        lowRamColumn.setCellValueFactory(new PropertyValueFactory<>("ram"));
        lowDiskColumn.setCellValueFactory(new PropertyValueFactory<>("disk"));
        lowPerformanceColumn.setCellValueFactory(new PropertyValueFactory<>("performanceType"));
        lowStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        lowPerformanceTable.setItems(viewModel.getLowPerformanceLaptops());
    }

    @FXML
    private void onCreateLaptop(ActionEvent event) {
        viewHandler.openCreateLaptopView();
    }

    @Override
    public void close() {
        // Unbind properties to avoid memory leaks
        totalAvailableCountLabel.textProperty().unbind();
        highPerformanceCountLabel.textProperty().unbind();
        lowPerformanceCountLabel.textProperty().unbind();
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        viewModel.refreshLaptops();
    }

    @FXML
    private void onBack(ActionEvent event) {
        viewHandler.openLaptopManagementMenu();
    }

    @FXML
    private void onExit(ActionEvent event) {
        close();
        viewHandler.exitApplication();
    }
}