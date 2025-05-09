package core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.controllers.ViewController;

import java.io.IOException;

public class ViewHandler {
    ViewmModelFactory viewmModelFactory;
    private Scene createStudentScene;
    private Scene laptopManagementMenuScene;
    private Stage primaryStage;
    private Stage secondaryStage;

    public ViewHandler(){
        viewmModelFactory = ViewmModelFactory.getInstance();
        primaryStage = new Stage();
        secondaryStage = new Stage();
        primaryStage.setResizable(false);
        secondaryStage.setResizable(false);
    }

    // Main metode start
    public void startStaff(){
        openLaptopManagementMenu();
    }

    // De forskellige Scener og deres åbning

    public void openCreateStudent(){
        try {
            Parent root = loadFXML("/view/fxmlFiler/createStudent.fxml");
            createStudentScene = new Scene(root);
            primaryStage.setScene(createStudentScene);
            primaryStage.setTitle("VIA Laptop Udlånssystem - Opret Student");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openLaptopManagementMenu() {
        try {
            Parent root = loadFXML("/view/fxmlFiler/LaptopManagementMenu.fxml");
            laptopManagementMenuScene = new Scene(root);
            primaryStage.setScene(laptopManagementMenuScene);
            primaryStage.setTitle("VIA Laptop Udlånssystem - Menu");
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openAvailableLaptopsView(){
        try {
            Parent root = loadFXML("/view/fxmlFiler/AvailableLaptopsView.fxml");
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Available laptops view");
            primaryStage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void openLoanOverView(){
        try {
            Parent root = loadFXML("/view/fxmlFiler/LoanOverView.fxml");
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("Loan Over View");
            primaryStage.show();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void openReturnLaptopView(){
        try {
            Parent root = loadFXML("/view/fxmlFiler/ReturnLaptopView.fxml");
            primaryStage.setScene(new Scene(root));
            primaryStage.setTitle("ReturnLaptopView");
            primaryStage.show();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Interne hjælpe metoder

    private Parent loadFXML(String path) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(path));
        Parent root = loader.load();
        ViewController controller = loader.getController();
        controller.init(this, viewmModelFactory);
        return root;
    }

    public void exitApplication(){
        System.exit(0);
    }
}
