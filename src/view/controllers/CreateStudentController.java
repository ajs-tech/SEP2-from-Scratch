package view.controllers;

import core.ViewHandler;
import core.ViewmModelFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.event.ActionEvent;
import viewmodel.CreateStudentViewModel;

public class CreateStudentController implements ViewController{

    private ViewHandler viewHandler;
    private CreateStudentViewModel createStudentViewModel;

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
    @FXML private TableView<?> highPerformanceLaptopsTable;
    @FXML private TableColumn<?, ?> highLaptopBrandColumn;
    @FXML private TableColumn<?, ?> highLaptopModelColumn;
    @FXML private TableColumn<?, ?> highLaptopRamColumn;
    @FXML private TableColumn<?, ?> highLaptopDiskColumn;
    @FXML private TableColumn<?, ?> highLaptopStatusColumn;
    @FXML private TableColumn<?, ?> highLaptopStudentColumn;

    // TableViews and TableColumns for low-performance laptops
    @FXML private TableView<?> lowPerformanceLaptopsTable;
    @FXML private TableColumn<?, ?> lowLaptopBrandColumn;
    @FXML private TableColumn<?, ?> lowLaptopModelColumn;
    @FXML private TableColumn<?, ?> lowLaptopRamColumn;
    @FXML private TableColumn<?, ?> lowLaptopDiskColumn;
    @FXML private TableColumn<?, ?> lowLaptopStatusColumn;
    @FXML private TableColumn<?, ?> lowLaptopStudentColumn;

    // TableViews and TableColumns for high-performance queue
    @FXML private TableView<?> highPerformanceQueueTable;
    @FXML private TableColumn<?, ?> highQueueViaIdColumn;
    @FXML private TableColumn<?, ?> highQueueNameColumn;
    @FXML private TableColumn<?, ?> highQueueEmailColumn;
    @FXML private TableColumn<?, ?> highQueuePhoneColumn;
    @FXML private TableColumn<?, ?> highQueueDateColumn;

    // TableViews and TableColumns for low-performance queue
    @FXML private TableView<?> lowPerformanceQueueTable;
    @FXML private TableColumn<?, ?> lowQueueViaIdColumn;
    @FXML private TableColumn<?, ?> lowQueueNameColumn;
    @FXML private TableColumn<?, ?> lowQueueEmailColumn;
    @FXML private TableColumn<?, ?> lowQueuePhoneColumn;
    @FXML private TableColumn<?, ?> lowQueueDateColumn;


    @Override
    public void init(ViewHandler viewHandler, ViewmModelFactory viewmModelFactory) {
        this.viewHandler = viewHandler;
        this.createStudentViewModel = viewmModelFactory.getCreateStudentViewModel();

    }

    @Override
    public void close() {

    }


    // Event handler methods
    @FXML
    private void onCreateStudent(ActionEvent event) {
        // Implement logic to create a student and assign a laptop
    }

    @FXML
    private void onClearStudentForm(ActionEvent event) {
        // Implement logic to clear the student registration form
    }

    @FXML
    private void onRefreshLaptops(ActionEvent event) {
        // Implement logic to refresh the list of available laptops
    }

    @FXML
    private void onRefreshQueues(ActionEvent event) {
        // Implement logic to refresh the waiting lists
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
