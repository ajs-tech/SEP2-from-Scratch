package application;

import core.ViewHandler;
import javafx.application.Application;
import javafx.stage.Stage;

public class StaffApp extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        ViewHandler viewHandler = new ViewHandler();
        viewHandler.startStaff();
    }
}
