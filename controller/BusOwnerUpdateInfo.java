package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Request;
import model.Response;
import util.BackToPrevScene;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BusOwnerUpdateInfo {

    @FXML private TextField nameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Button confirmButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private String busId;
    private String currentName;

    @FXML
    public void initialize() {
        confirmButton.setOnAction(event -> handleConfirm());
        cancelButton.setOnAction(event -> {
            try {
                handleCancel();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
        nameField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        confirmPasswordField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
    }

    private void handleConfirm() {
        String newName = nameField.getText().trim();
        String newPassword = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (newName.isEmpty() && newPassword.isEmpty()) {
            showError("Please enter either a new name or password");
            return;
        }
        if (newPassword.isEmpty()||!newPassword.equals(confirmPassword)) {
            showError("Passwords can be null,Passwords do not match");
            return;
        }
        updateBusOwnerInfo(newName, newPassword);
    }

    private void updateBusOwnerInfo(String newName, String newPassword) {
        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            String updateData = busId + "," + newPassword + "," + newName;

            Request request = new Request("BusOwner", "UpdateInfo", updateData);
            oos.writeObject(request);
            Response response = (Response) ois.readObject();

            if (response.isSuccess()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BusOwnerHomePage.fxml"));
                Parent root = loader.load();
                BusOwnerHomePage controller = loader.getController();
                controller.setBusOwner(busId, newName); // No socket needed anymore
                Stage stage = (Stage) errorLabel.getScene().getWindow();
                stage.setScene(new Scene(root));
                root.getStylesheets().add(getClass().getResource("/view/BusOwnerHomePage.css").toExternalForm());
            } else {
                showError("Failed to update info on server");
            }

        } catch (IOException | ClassNotFoundException e) {
            showError("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void handleCancel() throws IOException, ClassNotFoundException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BusOwnerHomePage.fxml"));
        Parent root = loader.load();
        BusOwnerHomePage controller = loader.getController();
        controller.setBusOwner(busId, currentName);
        Stage stage = (Stage) errorLabel.getScene().getWindow();
        stage.setScene(new Scene(root));
        root.getStylesheets().add(getClass().getResource("/view/BusOwnerHomePage.css").toExternalForm());
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void clearError() {
        errorLabel.setText("");
        errorLabel.setVisible(false);
    }

    // Setters for initial data
    public void setBusOwner(String busId, String currentName) {
        this.busId = busId;
        this.currentName = currentName;
        nameField.setPromptText(currentName);
    }


}