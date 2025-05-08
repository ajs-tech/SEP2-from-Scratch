package view;

import core.ViewHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;

public class ReturnLaptopController implements ViewController {

    private ViewHandler viewHandler;

    // Søgepanel
    @FXML private ComboBox<String> searchTypeComboBox;
    @FXML private TextField searchField;
    @FXML private Button searchButton;
    @FXML private Button clearSearchButton;
    @FXML private Label searchErrorLabel;

    // TableView
    @FXML private TableView<?> studentLaptopTable;
    @FXML private TableColumn<?, ?> viaIdColumn;
    @FXML private TableColumn<?, ?> studentNameColumn;
    @FXML private TableColumn<?, ?> emailColumn;
    @FXML private TableColumn<?, ?> phoneColumn;
    @FXML private TableColumn<?, ?> laptopBrandColumn;
    @FXML private TableColumn<?, ?> laptopModelColumn;
    @FXML private TableColumn<?, ?> laptopSpecsColumn;
    @FXML private TableColumn<?, ?> loanDateColumn;

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
    public void init(ViewHandler viewHandler) {
        this.viewHandler = viewHandler;
    }

    @Override
    public void close() {
        // Her kan du rydde op hvis nødvendigt
    }

    // Event handler for "Søg"
    @FXML
    private void onSearchStudent(ActionEvent event) {
        // TODO: Implementér søgefunktion
    }

    // Event handler for "Ryd søgning"
    @FXML
    private void onClearSearch(ActionEvent event) {
        // TODO: Ryd søgefelter og opdater status
    }

    // Event handler for "Opdater"
    @FXML
    private void onRefresh(ActionEvent event) {
        // TODO: Opdater tabel med aktive udlån
    }

    // Event handler for "Returnér computer"
    @FXML
    private void onReturnComputer(ActionEvent event) {
        // TODO: Marker som returneret og vis i resultPanel
    }

    @FXML
    private void onRefreshList(ActionEvent event) {
        // TODO: Marker som returneret og vis i resultPanel
    }



    // Event handler for "Tilbage"
    @FXML
    private void onBack(ActionEvent event) {
        viewHandler.openLaptopManagementMenu();
    }

    // Event handler for "Afslut"
    @FXML
    private void onExit(ActionEvent event) {
        close();
        viewHandler.exitApplication();
    }
}