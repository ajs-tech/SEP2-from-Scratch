package view.controllers;

import core.ViewHandler;
import core.ViewmModelFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import viewmodel.ReturnLaptopViewModel;
import viewmodel.ReturnLaptopViewModel.LoanTableItem;

public class ReturnLaptopController implements ViewController {

    private ViewHandler viewHandler;
    private ReturnLaptopViewModel viewModel;

    // Søgepanel
    @FXML private ComboBox<String> searchTypeComboBox;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;
    @FXML private Label searchErrorLabel;

    // TableView
    @FXML private TableView<LoanTableItem> studentLaptopTable;
    @FXML private TableColumn<LoanTableItem, String> viaIdColumn;
    @FXML private TableColumn<LoanTableItem, String> studentNameColumn;
    @FXML private TableColumn<LoanTableItem, String> emailColumn;
    @FXML private TableColumn<LoanTableItem, String> phoneColumn;
    @FXML private TableColumn<LoanTableItem, String> laptopBrandColumn;
    @FXML private TableColumn<LoanTableItem, String> laptopModelColumn;
    @FXML private TableColumn<LoanTableItem, String> laptopSpecsColumn;
    @FXML private TableColumn<LoanTableItem, String> loanDateColumn;

    // Result panel
    @FXML private VBox returnResultPanel;
    @FXML private Label resultStudentLabel;
    @FXML private Label resultComputerLabel;
    @FXML private Label resultDateLabel;
    @FXML private Label resultStatusLabel;

    // Status og knapper
    @FXML private Label statusLabel;
    @FXML private Button refreshButton;
    @FXML private Button returnButton;
    @FXML private Button backButton;
    @FXML private Button exitButton;

    @Override
    public void init(ViewHandler viewHandler, ViewmModelFactory viewModelFactory) {
        this.viewHandler = viewHandler;
        this.viewModel = viewModelFactory.getReturnLaptopViewModel();

        setupBindings();
        setupTable();

        // Setup selection listener for the table
        studentLaptopTable.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> viewModel.setSelectedLoan(newVal));
    }

    private void setupBindings() {
        // Bind search properties
        searchTypeComboBox.valueProperty().bindBidirectional(viewModel.searchTypeProperty());
        searchField.textProperty().bindBidirectional(viewModel.searchTermProperty());
        searchErrorLabel.textProperty().bind(viewModel.searchErrorProperty());

        // Bind result labels
        resultStudentLabel.textProperty().bind(viewModel.resultStudentProperty());
        resultComputerLabel.textProperty().bind(viewModel.resultComputerProperty());
        resultDateLabel.textProperty().bind(viewModel.resultDateProperty());
        resultStatusLabel.textProperty().bind(viewModel.resultStatusProperty());

        // Bind status label
        statusLabel.textProperty().bind(viewModel.statusProperty());

        // Disable return button if no reservation is selected
        returnButton.disableProperty().bind(
                studentLaptopTable.getSelectionModel().selectedItemProperty().isNull());
    }

    private void setupTable() {
        // Setup table columns
        viaIdColumn.setCellValueFactory(new PropertyValueFactory<>("viaId"));
        studentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        laptopBrandColumn.setCellValueFactory(new PropertyValueFactory<>("laptopBrand"));
        laptopModelColumn.setCellValueFactory(new PropertyValueFactory<>("laptopModel"));
        laptopSpecsColumn.setCellValueFactory(new PropertyValueFactory<>("laptopSpecs"));
        loanDateColumn.setCellValueFactory(new PropertyValueFactory<>("loanDate"));

        // Bind table items to view model
        studentLaptopTable.setItems(viewModel.getActiveLoans());
    }

    @Override
    public void close() {
        // Unbind properties to avoid memory leaks
        searchTypeComboBox.valueProperty().unbindBidirectional(viewModel.searchTypeProperty());
        searchField.textProperty().unbindBidirectional(viewModel.searchTermProperty());
        searchErrorLabel.textProperty().unbind();

        resultStudentLabel.textProperty().unbind();
        resultComputerLabel.textProperty().unbind();
        resultDateLabel.textProperty().unbind();
        resultStatusLabel.textProperty().unbind();

        statusLabel.textProperty().unbind();
        returnButton.disableProperty().unbind();
    }

    @FXML
    private void onSearchStudent(ActionEvent event) {
        viewModel.searchStudent();
    }

    @FXML
    private void onClearSearch(ActionEvent event) {
        viewModel.clearSearch();
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        viewModel.refreshLoanList();
    }

    @FXML
    private void onRefreshList(ActionEvent event) {
        viewModel.refreshLoanList();
    }

    @FXML
    private void onReturnComputer(ActionEvent event) {
        boolean success = viewModel.returnComputer();
        if (success) {
            studentLaptopTable.getSelectionModel().clearSelection();
        }
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