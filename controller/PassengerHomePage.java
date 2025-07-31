package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import model.Request;
import model.Response;
import util.BackToPrevScene;
import util.Notification;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PassengerHomePage {
    @FXML private Label welcomeLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label statusLabel;
    @FXML private Button logoutButton;
    @FXML private Button changeCredentialsButton;
    @FXML private Button selectLocationButton;
    @FXML private Button reportIssueButton;
    private String name;
    private String email;

    public void setPassenger(String email, String name) {
        this.email = email;
        this.name = name;
        initialize();
    }

    @FXML
    private void initialize() {
        welcomeLabel.setText("Welcome " + name);
        nameLabel.setText(name);
        emailLabel.setText(email);
    }

    @FXML
    private void handleChangeCredentials() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PassengerResetPassword.fxml"));
            Parent root = loader.load();
            PassengerResetPassword controller = loader.getController();
            controller.setUserEmail(email);

            Stage stage = (Stage) emailLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            String css = this.getClass().getResource("/view/PassengerResetPassword.css").toExternalForm();
            root.getStylesheets().add(css);

        } catch (IOException e) {
            Notification.showError("Navigation Error", "Failed to load reset password page");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSelectLocation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PassengerLocationSelection.fxml"));
            Parent root = loader.load();
            PassengerLocationSelection controller = loader.getController();
            controller.setPassenger(email,name);
            Stage stage = (Stage) emailLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            String css = this.getClass().getResource("/view/PassengerLocationSelection.css").toExternalForm();
            root.getStylesheets().add(css);

        } catch (IOException e) {
            Notification.showError("Navigation Error", "Failed to load location selection page");
            e.printStackTrace();
        }
    }

    @FXML
    void handleNotification(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PassengerNotification.fxml"));
            Parent root = loader.load();
            PassengerNotification controller = loader.getController();
            controller.setPassenger(email,name);
            Stage stage = (Stage) emailLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            String css = this.getClass().getResource("/view/PassengerNotification.css").toExternalForm();
            root.getStylesheets().add(css);

        } catch (IOException e) {
            Notification.showError("Navigation Error", "Failed to load report page");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleReportIssue() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PassengerReport.fxml"));
            Parent root = loader.load();
            PassengerReport controller = loader.getController();
            controller.setPassenger(email,name);
            Stage stage = (Stage) emailLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            String css = this.getClass().getResource("/view/PassengerReport.css").toExternalForm();
            root.getStylesheets().add(css);

        } catch (IOException e) {
            Notification.showError("Navigation Error", "Failed to load report page");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        // NETWORK UPDATE: Notify server about logout
        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            Request request = new Request("Passenger", "LogOut", email);
            oos.writeObject(request);
            // We don't really need the response for logout

            BackToPrevScene.navigateTo(logoutButton, "/view/PassengerLogIn.fxml", "/view/PassengerLogIn.css");
        } catch (IOException e) {
            Notification.showError("Logout Error", "Failed to properly logout");
            e.printStackTrace();
            // Still navigate back even if logout failed
            BackToPrevScene.navigateTo(logoutButton, "/view/PassengerLogIn.fxml", "/view/PassengerLogIn.css");
        }
    }
}