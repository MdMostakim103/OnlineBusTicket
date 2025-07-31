package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import util.BackToPrevScene;

import java.io.IOException;

public class AdminHomePage {
    private String name;


    @FXML private Label adminNameLabel;
    @FXML private void handleLogout() {
        BackToPrevScene.navigateTo(adminNameLabel, "/view/AdminLogIn.fxml", "/view/AdminLogIn.css");
    }

    @FXML
    private void handleNotifications() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminNotification.fxml"));
        Parent root = loader.load();
        AdminNotification controller = loader.getController();
        controller.setAdmin(name);

        Stage stage = (Stage) (adminNameLabel).getScene().getWindow();
        stage.setScene(new Scene(root));
        root.getStylesheets().add(getClass().getResource("/view/AdminNotification.css").toExternalForm());
    }

    @FXML
    private void handleContactBusOwner() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminContactBusOwner.fxml"));
        Parent root = loader.load();
        AdminContactBusOwner controller = loader.getController();
        controller.setAdmin(name);

        Stage stage = (Stage) (adminNameLabel).getScene().getWindow();
        stage.setScene(new Scene(root));
        root.getStylesheets().add(getClass().getResource("/view/AdminContactBusOwner.css").toExternalForm());
    }

    @FXML
    private void handleContactPassenger() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminContactPassenger.fxml"));
        Parent root = loader.load();
        AdminContactPassenger controller = loader.getController();
        controller.setAdmin(name);

        Stage stage = (Stage) (adminNameLabel).getScene().getWindow();
        stage.setScene(new Scene(root));
        root.getStylesheets().add(getClass().getResource("/view/AdminContactPassenger.css").toExternalForm());
    }

    public void setAdmin(String name) {
        this.name = name;
        if (adminNameLabel != null) {
            adminNameLabel.setText("Admin: " + name);
        }
    }

}
