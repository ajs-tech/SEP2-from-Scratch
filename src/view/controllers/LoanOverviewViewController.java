package view.controllers;

import core.ViewHandler;
import core.ViewmModelFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import viewmodel.LoanOverviewViewModel;
import viewmodel.LoanOverviewViewModel.LoanTableItem;

public class LoanOverviewViewController implements ViewController {

    private ViewHandler viewHandler;
    private LoanOverviewViewModel viewModel;

    // Statistik Labels
    @FXML private Label activeLoansCountLabel;
    @FXML private Label highPerformanceCountLabel;
    @FXML private Label lowPerformanceCountLabel;

    // Høj-ydelses TableView og Columns
    @FXML private TableView<LoanTableItem> highPerformanceTable;
    @FXML private TableColumn<LoanTableItem, String> highStudentNameColumn;
    @FXML private TableColumn<LoanTableItem, String> highViaIdColumn;
    @FXML private TableColumn<LoanTableItem, String> highEmailColumn;
    @FXML private TableColumn<LoanTableItem, String> highPhoneColumn;
    @FXML private TableColumn<LoanTableItem, String> highLaptopBrandColumn;
    @FXML private TableColumn<LoanTableItem, String> highLaptopModelColumn;
    @FXML private TableColumn<LoanTableItem, String> highSpecsColumn;
    @FXML private TableColumn<LoanTableItem, String> highLoanDateColumn;

    // Lav-ydelses TableView og Columns
    @FXML private TableView<LoanTableItem> lowPerformanceTable;
    @FXML private TableColumn<LoanTableItem, String> lowStudentNameColumn;
    @FXML private TableColumn<LoanTableItem, String> lowViaIdColumn;
    @FXML private TableColumn<LoanTableItem, String> lowEmailColumn;
    @FXML private TableColumn<LoanTableItem, String> lowPhoneColumn;
    @FXML private TableColumn<LoanTableItem, String> lowLaptopBrandColumn;
    @FXML private TableColumn<LoanTableItem, String> lowLaptopModelColumn;
    @FXML private TableColumn<LoanTableItem, String> lowSpecsColumn;
    @FXML private TableColumn<LoanTableItem, String> lowLoanDateColumn;

    // Buttons
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button exitButton;

    @Override
    public void init(ViewHandler viewHandler, ViewmModelFactory viewModelFactory) {
        this.viewHandler = viewHandler;
        this.viewModel = viewModelFactory.getLoanOverviewViewModel();

        setupBindings();
        setupTables();
    }

    private void setupBindings() {
        // Bind statistics labels
        activeLoansCountLabel.textProperty().bind(viewModel.activeLoansCountProperty().asString("%d"));
        highPerformanceCountLabel.textProperty().bind(viewModel.highPerformanceCountProperty().asString("%d"));
        lowPerformanceCountLabel.textProperty().bind(viewModel.lowPerformanceCountProperty().asString("%d"));
    }

    private void setupTables() {
        // Setup high-performance table
        highStudentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        highViaIdColumn.setCellValueFactory(new PropertyValueFactory<>("viaId"));
        highEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        highPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        highLaptopBrandColumn.setCellValueFactory(new PropertyValueFactory<>("laptopBrand"));
        highLaptopModelColumn.setCellValueFactory(new PropertyValueFactory<>("laptopModel"));
        highSpecsColumn.setCellValueFactory(new PropertyValueFactory<>("specs"));
        highLoanDateColumn.setCellValueFactory(new PropertyValueFactory<>("loanDate"));

        highPerformanceTable.setItems(viewModel.getHighPerformanceLoans());

        // Setup low-performance table
        lowStudentNameColumn.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        lowViaIdColumn.setCellValueFactory(new PropertyValueFactory<>("viaId"));
        lowEmailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        lowPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("phone"));
        lowLaptopBrandColumn.setCellValueFactory(new PropertyValueFactory<>("laptopBrand"));
        lowLaptopModelColumn.setCellValueFactory(new PropertyValueFactory<>("laptopModel"));
        lowSpecsColumn.setCellValueFactory(new PropertyValueFactory<>("specs"));
        lowLoanDateColumn.setCellValueFactory(new PropertyValueFactory<>("loanDate"));

        lowPerformanceTable.setItems(viewModel.getLowPerformanceLoans());
    }

    @Override
    public void close() {
        // Unbind properties to avoid memory leaks
        activeLoansCountLabel.textProperty().unbind();
        highPerformanceCountLabel.textProperty().unbind();
        lowPerformanceCountLabel.textProperty().unbind();
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        viewModel.refreshLoanOverview();
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