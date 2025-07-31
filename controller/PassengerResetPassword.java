package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import model.Passenger;
import model.Request;
import model.Response;
import util.BackToPrevScene;
import util.Notification;
import util.Validation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PassengerResetPassword {
    @FXML private TextField userNameTextfield;
    @FXML private PasswordField passwordTextfield;
    @FXML private PasswordField confirmPasswordTextfield;
    @FXML private Label errorLabel;
    @FXML private Button confirmButton;
    @FXML private Button backButton;

    String email;

    public void setUserEmail(String email){
        this.email = email;
    }

    @FXML
    private void handleReset() throws IOException {
        String username = userNameTextfield.getText().trim();
        String password = passwordTextfield.getText();
        String confirmPassword = confirmPasswordTextfield.getText();

        // Validate inputs
        if (username.isEmpty()) {
            errorLabel.setText("Please enter your username or email");
            return;
        }

        if (password.isEmpty()) {
            errorLabel.setText("Please enter a new password");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            return;
        }

        // NETWORK UPDATE: Changed from FileHandler to network call
        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            String data = email + "," + username + "," + password;
            Request request = new Request("Passenger", "ForgetPassword", data);
            oos.writeObject(request);
            Response response = (Response) ois.readObject();

            if (response.isSuccess()) {
                Notification.showSuccess("Password Reset",
                        "Your password has been successfully updated");

                userNameTextfield.clear();
                passwordTextfield.clear();
                confirmPasswordTextfield.clear();

                BackToPrevScene.navigateTo(backButton, "/view/PassengerLogIn.fxml", "/view/PassengerLogIn.css");
            } else {
                errorLabel.setText("Password reset failed");
            }
        } catch (ClassNotFoundException e) {
            Notification.showError("Error", "An error occurred during password reset");
            e.printStackTrace();
        }
    }

    @FXML
    private void backToPrevScene() throws IOException {
        BackToPrevScene.navigateTo(backButton, "/view/PassengerLogIn.fxml", "/view/PassengerLogIn.css");
    }
}