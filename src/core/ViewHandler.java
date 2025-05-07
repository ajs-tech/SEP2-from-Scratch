package core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles view navigation and management in the application
 */
public class ViewHandler {
  // Constants for view identification
  public static final String MAIN_MENU = "MainMenu";
  public static final String STUDENT_LAPTOP = "StudentLaptop";
  public static final String RETURN_COMPUTER = "ReturnComputer";
  public static final String LOAN_OVERVIEW = "LoanOverview";
  public static final String AVAILABLE_LAPTOPS = "AvailableLaptops";
  public static final String CREATE_STUDENT = "CreateStudent";

  private Stage primaryStage;
  private final ViewModelFactory viewModelFactory;
  private final Map<String, Scene> scenes;

  /**
   * Creates a new ViewHandler
   * @param viewModelFactory The factory for creating ViewModels
   */
  public ViewHandler(ViewModelFactory viewModelFactory) {
    this.viewModelFactory = viewModelFactory;
    this.scenes = new HashMap<>();
    this.primaryStage = new Stage();
  }

  /**
   * Starts the application by opening the main menu
   */
  public void start() {
    openView(MAIN_MENU);
    primaryStage.setTitle("VIA Laptop Udlånssystem");
    primaryStage.show();
  }

  /**
   * Opens the main menu view
   */
  public void openMainMenu() {
    openView(MAIN_MENU);
  }

  /**
   * Opens the student laptop view
   */
  public void openStudentLaptopView() {
    openView(STUDENT_LAPTOP);
  }

  /**
   * Opens the create student view
   */
  public void openCreateStudentView() {
    openView(CREATE_STUDENT);
  }

  /**
   * Opens the return computer view
   */
  public void openReturnComputerView() {
    openView(RETURN_COMPUTER);
  }

  /**
   * Opens the loan overview view
   */
  public void openLoanOverviewView() {
    openView(LOAN_OVERVIEW);
  }

  /**
   * Opens the available laptops view
   */
  public void openAvailableLaptopsView() {
    openView(AVAILABLE_LAPTOPS);
  }

  /**
   * Closes all views
   */
  public void closeView() {
    primaryStage.close();
  }

  /**
   * Generic method to open any view
   * @param viewId The ID of the view to open
   */
  private void openView(String viewId) {
    Scene scene = scenes.get(viewId);
    if (scene == null) {
      scene = loadView(viewId);
      scenes.put(viewId, scene);
    }
    primaryStage.setScene(scene);
    primaryStage.sizeToScene();
  }

  /**
   * Loads a view from FXML and initializes its controller
   * @param viewId The ID of the view to load
   * @return The loaded Scene
   */
  private Scene loadView(String viewId) {
    try {
      FXMLLoader loader = new FXMLLoader();
      Parent root = null;
      ViewController controller = null;
      Object viewModel = null;

      switch (viewId) {
        case MAIN_MENU:
          loader.setLocation(getClass().getResource("/view/fxmlFiler/LaptopManagementMenu.fxml"));
          root = loader.load();
          controller = loader.getController();
          viewModel = viewModelFactory.getLaptopManagementMenuViewModel();
          break;
        case STUDENT_LAPTOP:
          loader.setLocation(getClass().getResource("/view/fxmlFiler/StudentLaptopView.fxml"));
          root = loader.load();
          controller = loader.getController();
          viewModel = viewModelFactory.getStudentLaptopViewModel();
          break;
        case CREATE_STUDENT:
          loader.setLocation(getClass().getResource("/view/fxmlFiler/createStudent.fxml"));
          root = loader.load();
          controller = loader.getController();
          viewModel = viewModelFactory.getCreateStudentViewModel();
          break;
        case RETURN_COMPUTER:
          loader.setLocation(getClass().getResource("/view/fxmlFiler/ReturnComputerView.fxml"));
          root = loader.load();
          controller = loader.getController();
          viewModel = viewModelFactory.getReturnComputerViewModel();
          break;
        case LOAN_OVERVIEW:
          loader.setLocation(getClass().getResource("/view/fxmlFiler/LoanOverView.fxml"));
          root = loader.load();
          controller = loader.getController();
          viewModel = viewModelFactory.getLoanOverviewViewModel();
          break;
        case AVAILABLE_LAPTOPS:
          loader.setLocation(getClass().getResource("/view/fxmlFiler/AvailableLaptopsView.fxml"));
          root = loader.load();
          controller = loader.getController();
          viewModel = viewModelFactory.getAvailableLaptopsViewModel();
          break;
        default:
          throw new IllegalArgumentException("Unknown view: " + viewId);
      }

      // Initialize the controller with the view handler and view model
      controller.init(this, viewModel);

      // Return the loaded scene
      return new Scene(root);
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}