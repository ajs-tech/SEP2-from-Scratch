package view.controllers;

import core.ViewHandler;
import core.ViewmModelFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Button;
import javafx.event.ActionEvent;
import viewmodel.LoanOverviewViewModel;

public class LoanOverviewViewController implements ViewController {

    private ViewHandler viewHandler;
    private LoanOverviewViewModel loanOverviewViewModel;

    // Statistik Labels
    @FXML private Label activeLoansCountLabel;
    @FXML private Label highPerformanceCountLabel;
    @FXML private Label lowPerformanceCountLabel;

    // Høj-ydelses TableView og Columns
    @FXML private TableView<?> highPerformanceTable;
    @FXML private TableColumn<?, ?> highStudentNameColumn;
    @FXML private TableColumn<?, ?> highViaIdColumn;
    @FXML private TableColumn<?, ?> highEmailColumn;
    @FXML private TableColumn<?, ?> highPhoneColumn;
    @FXML private TableColumn<?, ?> highLaptopBrandColumn;
    @FXML private TableColumn<?, ?> highLaptopModelColumn;
    @FXML private TableColumn<?, ?> highSpecsColumn;
    @FXML private TableColumn<?, ?> highLoanDateColumn;

    // Lav-ydelses TableView og Columns
    @FXML private TableView<?> lowPerformanceTable;
    @FXML private TableColumn<?, ?> lowStudentNameColumn;
    @FXML private TableColumn<?, ?> lowViaIdColumn;
    @FXML private TableColumn<?, ?> lowEmailColumn;
    @FXML private TableColumn<?, ?> lowPhoneColumn;
    @FXML private TableColumn<?, ?> lowLaptopBrandColumn;
    @FXML private TableColumn<?, ?> lowLaptopModelColumn;
    @FXML private TableColumn<?, ?> lowSpecsColumn;
    @FXML private TableColumn<?, ?> lowLoanDateColumn;

    // Buttons
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button exitButton;

    @Override
    public void init(ViewHandler viewHandler, ViewmModelFactory viewmModelFactory) {
        this.viewHandler = viewHandler;
        this.loanOverviewViewModel = viewmModelFactory.getLoanOverviewViewModel();
    }

    @Override
    public void close() {

    }

    @FXML
    private void onRefresh(ActionEvent event) {
        // Opdater tabel og statistikker
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
