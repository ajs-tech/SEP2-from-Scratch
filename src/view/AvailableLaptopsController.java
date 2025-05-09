package view;

import core.ViewHandler;
import core.ViewmModelFactory;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import viewmodel.AvailableLaptopsViewModel;

public class AvailableLaptopsController implements ViewController {

    private ViewHandler viewHandler;
    private AvailableLaptopsViewModel availableLaptopsViewModel;

    // Statistik labels
    @FXML private Label totalAvailableCountLabel;
    @FXML private Label highPerformanceCountLabel;
    @FXML private Label lowPerformanceCountLabel;

    // TableView for høj-ydelses computere
    @FXML private TableView<?> highPerformanceTable;
    @FXML private TableColumn<?, ?> highIdColumn;
    @FXML private TableColumn<?, ?> highBrandColumn;
    @FXML private TableColumn<?, ?> highModelColumn;
    @FXML private TableColumn<?, ?> highRamColumn;
    @FXML private TableColumn<?, ?> highDiskColumn;
    @FXML private TableColumn<?, ?> highPerformanceColumn;
    @FXML private TableColumn<?, ?> highStatusColumn;

    // TableView for lav-ydelses computere
    @FXML private TableView<?> lowPerformanceTable;
    @FXML private TableColumn<?, ?> lowIdColumn;
    @FXML private TableColumn<?, ?> lowBrandColumn;
    @FXML private TableColumn<?, ?> lowModelColumn;
    @FXML private TableColumn<?, ?> lowRamColumn;
    @FXML private TableColumn<?, ?> lowDiskColumn;
    @FXML private TableColumn<?, ?> lowPerformanceColumn;
    @FXML private TableColumn<?, ?> lowStatusColumn;

    // Knapper
    @FXML private Button refreshButton;
    @FXML private Button backButton;
    @FXML private Button exitButton;


    @Override
    public void init(ViewHandler viewHandler, ViewmModelFactory viewmModelFactory) {
        this.viewHandler = viewHandler;
        this.availableLaptopsViewModel = viewmModelFactory.getAvailableLaptopsViewModel();

    }

    @Override
    public void close() {

    }


    // Event handler for "Opdater oversigt"
    @FXML
    private void onRefresh(ActionEvent event) {
        // TODO: Opdater statistikker og tabelvisninger med data fra model/backend
    }

    // Event handler for "Tilbage til menu"
    @FXML
    private void onBack(ActionEvent event) {
        // TODO: Naviger tilbage til hovedmenu (scene-skift via view-handler f.eks.)
    }

    // Event handler for "Afslut"
    @FXML
    private void onExit(ActionEvent event) {
        System.exit(0);
    }


}
