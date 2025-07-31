package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.Node;
import model.Request;
import model.Response;
import util.BackToPrevScene;
import util.Notification;
import util.Validation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class PassengerSignup {
    private static final int CONNECTION_TIMEOUT = 5000;

    @FXML private TextField emailField;
    @FXML private TextField nameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label errorLabel;

    @FXML
    private void handleSignup() {
        if (!validateInputs()) {
            return;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new java.net.InetSocketAddress("localhost", 5063), CONNECTION_TIMEOUT);
            socket.setSoTimeout(CONNECTION_TIMEOUT);

            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());

            // Check email first
            Request checkRequest = new Request("Passenger", "CheckEmail", emailField.getText().trim());
            oos.writeObject(checkRequest);
            oos.flush();

            Response checkResponse = (Response) ois.readObject();
            if (checkResponse.isSuccess()) {
                errorLabel.setText("Email already registered");
                return;
            }

            // Proceed with registration
            Request signupRequest = new Request("Passenger", "SignUp",
                    String.join(",",
                            emailField.getText().trim(),
                            nameField.getText().trim(),
                            passwordField.getText().trim()
                    )
            );
            oos.writeObject(signupRequest);
            oos.flush();

            Response signupResponse = (Response) ois.readObject();
            if (signupResponse.isSuccess()) {
                Notification.showSuccess("Success", "Account created successfully!");
                clearFields();
                navigateToLogin();
            } else {
                errorLabel.setText("Registration failed: " + signupResponse.getData());
            }
        } catch (SocketTimeoutException e) {
            errorLabel.setText("Server response timeout");
        } catch (IOException e) {
            errorLabel.setText("Cannot connect to server");
        } catch (ClassNotFoundException e) {
            errorLabel.setText("Server communication error");
        }
    }

    private boolean validateInputs() {
        String email = emailField.getText().trim();
        String name = nameField.getText().trim();
        String password = passwordField.getText().trim();
        String confirmPassword = confirmPasswordField.getText().trim();

        if (email.isEmpty() || name.isEmpty() || password.isEmpty()) {
            errorLabel.setText("All fields are required");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            errorLabel.setText("Passwords do not match");
            return false;
        }

        if (!Validation.isValidEmail(email)) {
            errorLabel.setText("Invalid email format");
            return false;
        }

        return true;
    }

    private void clearFields() {
        emailField.clear();
        nameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private void navigateToLogin() {
        try {
            BackToPrevScene.navigateTo(emailField.getScene().getRoot(), "/view/PassengerLogIn.fxml", "/view/PassengerLogIn.css");
        } catch (Exception e) {
            showNavigationError();
        }
    }

    private void showNavigationError() {
        try {
            Notification.showError("Navigation Error", "Failed to load login screen");
        } catch (Exception e) {
            errorLabel.setText("Failed to load login screen");
        }
    }

    @FXML
    public void Login(ActionEvent event) {
        try {
            BackToPrevScene.navigateTo(
                    (Node) event.getSource(),
                    "/view/PassengerLogIn.fxml",
                    "/view/PassengerLogIn.css"
            );
        } catch (Exception e) {
            showNavigationError();
        }
    }
}