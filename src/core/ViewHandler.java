package core;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import view.ViewController;

import javax.imageio.IIOException;

public class ViewHandler {
    private Scene createStudentScene;
    private Stage primaryStage;
    private Stage secondaryStage;

    public ViewHandler(){
        primaryStage = new Stage();
        secondaryStage = new Stage();
        primaryStage.setResizable(false);
        primaryStage.setResizable(false);
    }

    public void startStaff(){
        openCreateStudent();
    }

    public void openCreateStudent(){
        createStudentScene = null;

        try {

        }
    }






    private Parent loadFXML(String path) throws IIOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(path));
        Parent root = loader.load();
        ViewController controller = loader.getController();

    }



}
