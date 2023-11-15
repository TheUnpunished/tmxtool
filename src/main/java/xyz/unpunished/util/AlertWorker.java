package xyz.unpunished.util;

import javafx.scene.control.Alert;
import xyz.unpunished.util.I18N;

public class AlertWorker {

    public static void showAlert(Alert.AlertType type, String headerText, String mainText){
        Alert alertToShow = new Alert(type);
        alertToShow.setTitle(I18N.get("tool_name"));
        alertToShow.setHeaderText(headerText);
        alertToShow.setContentText(mainText);
        alertToShow.showAndWait();
    }

}
