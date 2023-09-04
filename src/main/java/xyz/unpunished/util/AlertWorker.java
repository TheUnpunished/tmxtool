package xyz.unpunished.util;

import javafx.scene.control.Alert;

public class AlertWorker {

    public static void showAlert(Alert.AlertType type, String title, String headerText, String mainText){
        Alert alertToShow = new Alert(type);
        alertToShow.setTitle(title);
        alertToShow.setHeaderText(headerText);
        alertToShow.setContentText(mainText);
        alertToShow.showAndWait();
    }

}
