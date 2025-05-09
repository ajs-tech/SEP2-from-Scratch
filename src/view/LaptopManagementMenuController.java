package view;

import core.ViewHandler;
import core.ViewmModelFactory;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import viewmodel.LaptopManagementMenuViewModel;

public class LaptopManagementMenuController implements ViewController {
    private ViewHandler viewHandler;
    private LaptopManagementMenuViewModel laptopManagementMenuViewModel;
    // Labels

    @FXML
    private Label statusLabel;

    @FXML
    private Label availableLabel;

    @FXML
    private Label loanedLabel;


    // ViewController interface metoder

    @Override
    public void init(ViewHandler viewHandler, ViewmModelFactory viewmModelFactory) {
        this.viewHandler = viewHandler;
        this.laptopManagementMenuViewModel = viewmModelFactory.getLaptopManagementMenuViewModel();
        availableLabel.setText(availableLabel.getText() + " 10");
    }

    @Override
    public void close() {

    }


    // fxml handlinger

    @FXML
    public void handleCreateStudent(ActionEvent event) {
        // Open the loan laptop view
        // For example: viewHandler.openLoanLaptopView();
        System.out.println("create student button clicked");
        viewHandler.openCreateStudent();
    }

    @FXML
    public void handleReturnLaptop(ActionEvent event) {
        System.out.println("Return laptop button clicked");
        viewHandler.openReturnLaptopView();
    }

    @FXML
    public void handleLoanOverview(ActionEvent event) {
        System.out.println("Loan overview button clicked");
        viewHandler.openLoanOverView();
    }

    @FXML
    public void handleAvailableLaptops(ActionEvent event) {
        System.out.println("Available laptops button clicked");
        viewHandler.openAvailableLaptopsView();
    }

    @FXML
    public void handleExit(ActionEvent event) {
        // Exit the application
        close();
        viewHandler.exitApplication();
    }


}
