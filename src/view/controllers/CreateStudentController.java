package view.controllers;

import core.ViewHandler;
import core.ViewmModelFactory;
import enums.PerformanceTypeEnum;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import objects.Laptop;
import objects.Student;
import viewmodel.CreateStudentViewModel;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CreateStudentController implements ViewController {
    private ViewHandler viewHandler;
    private CreateStudentViewModel viewModel;

    // TextFields
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField viaIdField;
    @FXML private TextField phoneNumberField;
    @FXML private TextField degreeTitleField;

    // DatePicker
    @FXML private DatePicker degreeEndDatePicker;

    // RadioButtons and ToggleGroup
    @FXML private RadioButton lowPerformanceRadio;
    @FXML private RadioButton highPerformanceRadio;
    @FXML private ToggleGroup performanceGroup;

    // Buttons
    @FXML private Button createStudentButton;
    @FXML private Button clearStudentButton;
    @FXML private Button refreshLaptopsButton;
    @FXML private Button refreshQueuesButton;
    @FXML private Button backButton;
    @FXML private Button exitButton;

    // Labels
    @FXML private Label studentErrorLabel;
    @FXML private Label resultStudentLabel;
    @FXML private Label resultStatusLabel;
    @FXML private Label resultLaptopLabel;
    @FXML private Label statusLabel;

    // VBox for assignment result panel
    @FXML private VBox assignmentResultPanel;

    // TableViews and TableColumns for high-performance laptops
    @FXML private TableView<Laptop> highPerformanceLaptopsTable;
    @FXML private TableColumn<Laptop, String> highLaptopBrandColumn;
    @FXML private TableColumn<Laptop, String> highLaptopModelColumn;
    @FXML private TableColumn<Laptop, Integer> highLaptopRamColumn;
    @FXML private TableColumn<Laptop, Integer> highLaptopDiskColumn;
    @FXML private TableColumn<Laptop, String> highLaptopStatusColumn;
    @FXML private TableColumn<Laptop, String> highLaptopStudentColumn;

    // TableViews and TableColumns for low-performance laptops
    @FXML private TableView<Laptop> lowPerformanceLaptopsTable;
    @FXML private TableColumn<Laptop, String> lowLaptopBrandColumn;
    @FXML private TableColumn<Laptop, String> lowLaptopModelColumn;
    @FXML private TableColumn<Laptop, Integer> lowLaptopRamColumn;
    @FXML private TableColumn<Laptop, Integer> lowLaptopDiskColumn;
    @FXML private TableColumn<Laptop, String> lowLaptopStatusColumn;
    @FXML private TableColumn<Laptop, String> lowLaptopStudentColumn;

    // TableViews and TableColumns for high-performance queue
    @FXML private TableView<Student> highPerformanceQueueTable;
    @FXML private TableColumn<Student, Integer> highQueueViaIdColumn;
    @FXML private TableColumn<Student, String> highQueueNameColumn;
    @FXML private TableColumn<Student, String> highQueueEmailColumn;
    @FXML private TableColumn<Student, Integer> highQueuePhoneColumn;
    @FXML private TableColumn<Student, String> highQueueDateColumn;

    // TableViews and TableColumns for low-performance queue
    @FXML private TableView<Student> lowPerformanceQueueTable;
    @FXML private TableColumn<Student, Integer> lowQueueViaIdColumn;
    @FXML private TableColumn<Student, String> lowQueueNameColumn;
    @FXML private TableColumn<Student, String> lowQueueEmailColumn;
    @FXML private TableColumn<Student, Integer> lowQueuePhoneColumn;
    @FXML private TableColumn<Student, String> lowQueueDateColumn;

    @Override
    public void init(ViewHandler viewHandler, ViewmModelFactory viewModelFactory) {
        this.viewHandler = viewHandler;
        this.viewModel = viewModelFactory.getCreateStudentViewModel();

        setupBindings();
        setupTables();
    }

    private void setupBindings() {
        // Bind text fields
        nameField.textProperty().bindBidirectional(viewModel.nameProperty());
        emailField.textProperty().bindBidirectional(viewModel.emailProperty());
        viaIdField.textProperty().bindBidirectional(viewModel.viaIdProperty());
        phoneNumberField.textProperty().bindBidirectional(viewModel.phoneNumberProperty());
        degreeTitleField.textProperty().bindBidirectional(viewModel.degreeTitleProperty());

        // Bind date picker
        degreeEndDatePicker.valueProperty().bindBidirectional(viewModel.degreeEndDateProperty());

        // Bind radio buttons
        highPerformanceRadio.selectedProperty().bindBidirectional(viewModel.highPerformanceProperty());
        lowPerformanceRadio.selectedProperty().bind(viewModel.highPerformanceProperty().not());

        // Bind result labels
        resultStudentLabel.textProperty().bind(viewModel.resultStudentProperty());
        resultStatusLabel.textProperty().bind(viewModel.resultStatusProperty());
        resultLaptopLabel.textProperty().bind(viewModel.resultLaptopProperty());
        studentErrorLabel.textProperty().bind(viewModel.errorProperty());
    }

    private void setupTables() {
        // Setup high-performance laptops table
        highLaptopBrandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        highLaptopModelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        highLaptopRamColumn.setCellValueFactory(new PropertyValueFactory<>("ram"));
        highLaptopDiskColumn.setCellValueFactory(new PropertyValueFactory<>("gigabyte"));
        highLaptopStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isAvailable() ? "Tilgængelig" : "Udlånt"));
        highLaptopStudentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(""));

        highPerformanceLaptopsTable.setItems(viewModel.getHighPerformanceLaptops());

        // Setup low-performance laptops table
        lowLaptopBrandColumn.setCellValueFactory(new PropertyValueFactory<>("brand"));
        lowLaptopModelColumn.setCellValueFactory(new PropertyValueFactory<>("model"));
        lowLaptopRamColumn.setCellValueFactory(new PropertyValueFactory<>("ram"));
        lowLaptopDiskColumn.setCellValueFactory(new PropertyValueFactory<>("gigabyte"));
        lowLaptopStatusColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().isAvailable() ? "Tilgængelig" : "Udlånt"));
        lowLaptopStudentColumn.setCellValueFactory(cellData -> new SimpleStringProperty(""));

        lowPerformanceLaptopsTable.setItems(viewModel.getLowPerformanceLaptops());

        // Setup high-performance queue table
        highQueueViaIdColumn.setCellValueFactory(new PropertyValueFactory<>("viaId"));
        highQueueNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        highQueueEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        highQueuePhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        highQueueDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(""));

        highPerformanceQueueTable.setItems(viewModel.getHighPerformanceQueue());

        // Setup low-performance queue table
        lowQueueViaIdColumn.setCellValueFactory(new PropertyValueFactory<>("viaId"));
        lowQueueNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        lowQueueEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        lowQueuePhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phoneNumber"));
        lowQueueDateColumn.setCellValueFactory(cellData -> new SimpleStringProperty(""));

        lowPerformanceQueueTable.setItems(viewModel.getLowPerformanceQueue());
    }

    @Override
    public void close() {
        // Unbind properties to avoid memory leaks
        nameField.textProperty().unbindBidirectional(viewModel.nameProperty());
        emailField.textProperty().unbindBidirectional(viewModel.emailProperty());
        viaIdField.textProperty().unbindBidirectional(viewModel.viaIdProperty());
        phoneNumberField.textProperty().unbindBidirectional(viewModel.phoneNumberProperty());
        degreeTitleField.textProperty().unbindBidirectional(viewModel.degreeTitleProperty());
        degreeEndDatePicker.valueProperty().unbindBidirectional(viewModel.degreeEndDateProperty());
        highPerformanceRadio.selectedProperty().unbindBidirectional(viewModel.highPerformanceProperty());

        resultStudentLabel.textProperty().unbind();
        resultStatusLabel.textProperty().unbind();
        resultLaptopLabel.textProperty().unbind();
        studentErrorLabel.textProperty().unbind();
    }

    @FXML
    private void onCreateStudent(ActionEvent event) {
        viewModel.createStudent();
    }

    @FXML
    private void onClearStudentForm(ActionEvent event) {
        viewModel.clearForm();
    }

    @FXML
    private void onRefreshLaptops(ActionEvent event) {
        viewModel.refreshLaptops();
    }

    @FXML
    private void onRefreshQueues(ActionEvent event) {
        viewModel.refreshQueues();
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