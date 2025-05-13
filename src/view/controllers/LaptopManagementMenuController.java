package view.controllers;

import core.ViewHandler;
import core.ViewmModelFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import viewmodel.LaptopManagementMenuViewModel;

public class LaptopManagementMenuController implements ViewController {
    private ViewHandler viewHandler;
    private LaptopManagementMenuViewModel viewModel;

    @FXML
    private Label statusLabel;

    @FXML
    private Label availableLabel;

    @FXML
    private Label loanedLabel;

    @Override
    public void init(ViewHandler viewHandler, ViewmModelFactory viewModelFactory) {
        this.viewHandler = viewHandler;
        this.viewModel = viewModelFactory.getLaptopManagementMenuViewModel();

        // Bind labels to viewModel properties
        statusLabel.textProperty().bind(viewModel.statusProperty());
        availableLabel.textProperty().bind(viewModel.availableLaptopsProperty().asString("Tilgængelige computere: %d"));
        loanedLabel.textProperty().bind(viewModel.loanedLaptopsProperty().asString("Udlånte computere: %d"));
    }

    @Override
    public void close() {
        // Unbind properties to avoid memory leaks
        statusLabel.textProperty().unbind();
        availableLabel.textProperty().unbind();
        loanedLabel.textProperty().unbind();
    }

    @FXML
    public void handleCreateStudent(ActionEvent event) {
        viewHandler.openCreateStudent();
    }

    @FXML
    public void handleReturnLaptop(ActionEvent event) {
        viewHandler.openReturnLaptopView();
    }

    @FXML
    public void handleLoanOverview(ActionEvent event) {
        viewHandler.openLoanOverView();
    }

    @FXML
    public void handleAvailableLaptops(ActionEvent event) {
        viewHandler.openAvailableLaptopsView();
    }

    @FXML
    public void handleExit(ActionEvent event) {
        close();
        viewHandler.exitApplication();
    }
}