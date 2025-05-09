package core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.Controller.*;
import viewmodel.ViewModelFactory;

import java.io.IOException;

/**
 * Håndterer alle views i applikationen.
 * Ansvarlig for at indlæse FXML-filer, initialisere controllere,
 * og navigere mellem forskellige views.
 */
public class ViewHandler {

  private Stage primaryStage;
  private Scene currentScene;
  private ViewModelFactory viewModelFactory;

  // References til controllere
  private LaptopManagementMenuController menuController;
  private AvailableLaptopsViewController availableLaptopsController;
  private StudentLaptopViewController studentLaptopController;
  private LoanOverviewViewController loanOverviewController;
  private ReturnComputerViewController returnComputerController;

  /**
   * Konstruktør for ViewHandler.
   *
   * @param viewModelFactory Factory som giver adgang til alle ViewModels
   */
  public ViewHandler(ViewModelFactory viewModelFactory) {
    this.viewModelFactory = viewModelFactory;
  }

  /**
   * Starter applikationen og viser hovedmenuen.
   *
   * @param primaryStage JavaFX main stage
   */
  public void start(Stage primaryStage) {
    this.primaryStage = primaryStage;
    this.currentScene = new Scene(new Parent() {});
    primaryStage.setTitle("VIA Laptop Udlånssystem");
    openMainMenu();
    primaryStage.show();
  }

  /**
   * Åbner hovedmenuen.
   */
  public void openMainMenu() {
    FXMLLoader loader = new FXMLLoader();

    if (menuController == null) {
      Parent root = getRootByPath("../fxmlFiler/LaptopManagementMenu.fxml", loader);
      menuController = loader.getController();
      menuController.init(this, viewModelFactory);
    } else {
      menuController.reset();
    }

    primaryStage.setTitle("VIA Laptop Udlånssystem - Hovedmenu");
    setScene(menuController);
  }

  /**
   * Åbner visning af tilgængelige laptops.
   */
  public void openAvailableLaptops() {
    FXMLLoader loader = new FXMLLoader();

    if (availableLaptopsController == null) {
      Parent root = getRootByPath("../fxmlFiler/AvailableLaptopsView.fxml", loader);
      availableLaptopsController = loader.getController();
      availableLaptopsController.init(this, viewModelFactory);
    } else {
      availableLaptopsController.reset();
    }

    primaryStage.setTitle("VIA Laptop Udlånssystem - Tilgængelige Laptops");
    setScene(availableLaptopsController);
  }

  /**
   * Åbner studerende/laptop-visningen.
   */
  public void openStudentLaptopView() {
    FXMLLoader loader = new FXMLLoader();

    if (studentLaptopController == null) {
      Parent root = getRootByPath("../fxmlFiler/StudentLaptopView.fxml", loader);
      studentLaptopController = loader.getController();
      studentLaptopController.init(this, viewModelFactory);
    } else {
      studentLaptopController.reset();
    }

    primaryStage.setTitle("VIA Laptop Udlånssystem - Udlån Laptop");
    setScene(studentLaptopController);
  }

  /**
   * Åbner låneoversigten.
   */
  public void openLoanOverview() {
    FXMLLoader loader = new FXMLLoader();

    if (loanOverviewController == null) {
      Parent root = getRootByPath("../fxmlFiler/LoanOverviewView.fxml", loader);
      loanOverviewController = loader.getController();
      loanOverviewController.init(this, viewModelFactory);
    } else {
      loanOverviewController.reset();
    }

    primaryStage.setTitle("VIA Laptop Udlånssystem - Låneoversigt");
    setScene(loanOverviewController);
  }

  /**
   * Åbner returner computer visningen.
   */
  public void openReturnComputerView() {
    FXMLLoader loader = new FXMLLoader();

    if (returnComputerController == null) {
      Parent root = getRootByPath("../fxmlFiler/ReturnComputerView.fxml", loader);
      returnComputerController = loader.getController();
      returnComputerController.init(this, viewModelFactory);
    } else {
      returnComputerController.reset();
    }

    primaryStage.setTitle("VIA Laptop Udlånssystem - Returner Computer");
    setScene(returnComputerController);
  }

  /**
   * Afslutter applikationen.
   */
  public void closeApplication() {
    // Før applikationen lukkes, frigør eventuelle ressourcer
    primaryStage.close();
  }

  /**
   * Hjælpemetode til at indlæse FXML-filer og returnere Parent node.
   */
  private Parent getRootByPath(String path, FXMLLoader loader) {
    loader.setLocation(getClass().getResource(path));
    Parent root = null;
    try {
      root = loader.load();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return root;
  }

  /**
   * Opdaterer scenen med det nye view.
   */
  private void setScene(Controller controller) {
    // Vi antager at getRootByPath har opdateret loaderen som nu indeholder vores controller
    Scene rootScene = ((Parent)controller).getScene();
    if (rootScene == null) {
      // Dette er en simplifikation - i virkeligheden ville du have mere kompleks logik her
      // for at få Parent fra controlleren
      System.out.println("Scene er null, kan ikke sætte den");
      return;
    }

    currentScene = rootScene;
    primaryStage.setScene(currentScene);
    primaryStage.sizeToScene();
  }
}