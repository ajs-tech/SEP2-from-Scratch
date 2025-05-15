package view.controllers;

import core.ViewHandler;
import core.ViewmModelFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import viewmodel.CreateLaptopViewModel;
import viewmodel.CreateLaptopViewModel.LaptopTableItem;

public class CreateLaptopController implements ViewController {

    private ViewHandler viewHandler;
    private CreateLaptopViewModel viewModel;

    // Form fields
    @FXML private TextField brandField;
    @FXML private TextField modelField;
    @FXML private TextField ramField;
    @FXML private TextField diskField;
    @FXML private RadioButton lowPerformanceRadio;
    @FXML private RadioButton highPerformanceRadio;
    @FXML private ToggleGroup performanceGroup;

    // Buttons
    @FXML private Button createLaptopButton;
    @FXML private Button clearLaptopButton;
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button exitButton;

    // Labels
    @FXML private Label laptopErrorLabel;
    @FXML private Label statusLabel;
    @FXML private Label totalLaptopsLabel;
    @FXML private Label availableLaptopsLabel;
    @FXML private Label loanedLaptopsLabel;

    // Result panel labels
    @FXML private Label resultLaptopLabel;
    @FXML private Label resultPerformanceTypeLabel;
    @FXML private Label resultSpecsLabel;
    @FXML private Label resultStatusLabel;

    // Result panel
    @FXML private VBox resultPanel;

    // TableViews and columns for all laptops
    @FXML private TableView<LaptopTableItem> allLaptopsTable;
    @FXML private TableColumn<LaptopTableItem, String> allIdColumn;
    @FXML private TableColumn<LaptopTableItem, String> allBrandColumn;
    @FXML private TableColumn<LaptopTableItem, String> allModelColumn;
    @FXML private TableColumn<LaptopTableItem, Integer> allRamColumn;
    @FXML private TableColumn<LaptopTableItem, Integer> allDiskColumn;
    @FXML private TableColumn<LaptopTableItem, String> allPerformanceColumn;
    @FXML private TableColumn<LaptopTableItem, String> allStatusColumn;

    // TableViews and columns for available laptops
    @FXML private TableView<LaptopTableItem> availableLaptopsTable;
    @FXML private TableColumn<LaptopTableItem, String> availableIdColumn;
    @FXML private TableColumn<LaptopTableItem, String> availableBrandColumn;
    @FXML private TableColumn<LaptopTableItem, String> availableModelColumn;
    @FXML private TableColumn<LaptopTableItem, Integer> availableRamColumn;
    @FXML private TableColumn<LaptopTableItem, Integer> availableDiskColumn;
    @FXML private TableColumn<LaptopTableItem, String> availablePerformanceColumn;

    // TableViews and columns for loaned laptops
    @FXML private TableView<LaptopTableItem> loanedLaptopsTable;
    @FXML private TableColumn<LaptopTableItem, String> loanedIdColumn;
    @FXML private TableColumn<LaptopTableItem, String> loanedBrandColumn;
    @FXML private TableColumn<LaptopTableItem, String> loanedModelColumn;
    @FXML private TableColumn<LaptopTableItem, Integer> loanedRamColumn;
    @FXML private TableColumn<LaptopTableItem, Integer> loanedDiskColumn;
    @FXML private TableColumn<LaptopTableItem, String> loanedPerformanceColumn;

    @Override
    public void init(ViewHandler viewHandler, ViewmModelFactory viewModelFactory) {
        this.viewHandler = viewHandler;
        this.viewModel = viewModelFactory.getCreateLaptopViewModel();

        setupBindings();
        setupTables();

        viewModel.refreshLaptops();
    }

    private void setupBindings() {
        brandField.textProperty().bindBidirectional(viewModel.brandProperty());
        modelField.textProperty().bindBidirectional(viewModel.modelProperty());
        ramField.textProperty().bindBidirectional(viewModel.ramProperty());
        diskField.textProperty().bindBidirectional(viewModel.diskProperty());
        highPerformanceRadio.selectedProperty().bindBidirectional(viewModel.highPerformanceProperty());
        lowPerformanceRadio.selectedProperty().bind(viewModel.highPerformanceProperty().not());

        laptopErrorLabel.textProperty().bind(viewModel.errorProperty());
        statusLabel.textProperty().bind(viewModel.statusProperty());

        totalLaptopsLabel.textProperty().bind(viewModel.totalLaptopsProperty().asString("%d"));
        availableLaptopsLabel.textProperty().bind(viewModel.availableLaptopsCountProperty().asString("%d"));
        loanedLaptopsLabel.textProperty().bind(viewModel.loanedLaptopsCountProperty().asString("%d"));

        resultLaptopLabel.textProperty().bind(viewModel.resultLaptopProperty());
        resultPerformanceTypeLabel.textProperty().bind(viewModel.resultPerformanceTypeProperty());
        resultSpecsLabel.textProperty().bind(viewModel.resultSpecsProperty());
        resultStatusLabel.textProperty().bind(viewModel.resultStatusProperty());
    }

    private void setupTables() {
        allIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        allBrandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        allModelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        allRamColumn.setCellValueFactory(new PropertyValueFactory<>("ram"));
        allDiskColumn.setCellValueFactory(new PropertyValueFactory<>("disk"));
        allPerformanceColumn.setCellValueFactory(new PropertyValueFactory<>("performanceType"));
        allStatusColumn.setCellValueFactory(new PropertyValueFactory<>("status"));

        allLaptopsTable.setItems(viewModel.getAllLaptops());

        availableIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        availableBrandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        availableModelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        availableRamColumn.setCellValueFactory(new PropertyValueFactory<>("ram"));
        availableDiskColumn.setCellValueFactory(new PropertyValueFactory<>("disk"));
        availablePerformanceColumn.setCellValueFactory(new PropertyValueFactory<>("performanceType"));

        availableLaptopsTable.setItems(viewModel.getAvailableLaptops());

        loanedIdColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        loanedBrandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        loanedModelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        loanedRamColumn.setCellValueFactory(new PropertyValueFactory<>("ram"));
        loanedDiskColumn.setCellValueFactory(new PropertyValueFactory<>("disk"));
        loanedPerformanceColumn.setCellValueFactory(new PropertyValueFactory<>("performanceType"));

        loanedLaptopsTable.setItems(viewModel.getLoanedLaptops());
    }

    @Override
    public void close() {
        brandField.textProperty().unbindBidirectional(viewModel.brandProperty());
        modelField.textProperty().unbindBidirectional(viewModel.modelProperty());
        ramField.textProperty().unbindBidirectional(viewModel.ramProperty());
        diskField.textProperty().unbindBidirectional(viewModel.diskProperty());
        highPerformanceRadio.selectedProperty().unbindBidirectional(viewModel.highPerformanceProperty());

        laptopErrorLabel.textProperty().unbind();
        statusLabel.textProperty().unbind();
        totalLaptopsLabel.textProperty().unbind();
        availableLaptopsLabel.textProperty().unbind();
        loanedLaptopsLabel.textProperty().unbind();

        resultLaptopLabel.textProperty().unbind();
        resultPerformanceTypeLabel.textProperty().unbind();
        resultSpecsLabel.textProperty().unbind();
        resultStatusLabel.textProperty().unbind();
    }

    @FXML
    private void onCreateLaptop(ActionEvent event) {
        boolean success = viewModel.createLaptop();
        if (success) {
            viewModel.refreshLaptops();
        }
    }

    @FXML
    private void onClearLaptopForm(ActionEvent event) {
        viewModel.clearForm();
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        viewModel.refreshLaptops();
    }

    @FXML
    private void onBack(ActionEvent event) {
        viewHandler.openAvailableLaptopsView();
    }

    @FXML
    private void onExit(ActionEvent event) {
        close();
        viewHandler.exitApplication();
    }
}