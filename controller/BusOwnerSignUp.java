package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.BusOwner;
import model.Request;
import model.Response;
import util.BackToPrevScene;


import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;

public class BusOwnerSignUp {

    @FXML private TextField busIdField;
    @FXML private TextField emailField;
    @FXML private TextField nameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;

    @FXML private Label busIdError;
    @FXML private Label emailError;
    @FXML private Label nameError;
    @FXML private Label passwordError;
    @FXML private Label confirmPasswordError;
    @FXML private Label formError;

    @FXML private Button backButton;
    @FXML private Button signUpButton;

    @FXML
    private void handleSignUp() throws IOException, ClassNotFoundException {
        clearErrors();

        String busId = busIdField.getText().trim();
        String email = emailField.getText().trim();
        String name = nameField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        boolean isValid = true;

//        if (busId.isEmpty()) {
//            busIdError.setText("Bus ID is required");
//            isValid = false;
//        } else if (BusDataHelper.busIdExists(busId)) {
//            busIdError.setText("Bus ID already exists");
//            isValid = false;
//        }
//        if (!Validation.isValidEmail(email)){
//            isValid=false;
//            emailError.setText("Invalid email format");
//        } else if (BusDataHelper.emailExists(email)) {
//            emailError.setText("Email already registered");
//            isValid = false;
//        }

//        if (name.isEmpty()) {
//            nameError.setText("Name is required");
//            isValid = false;
//        }
//        if (password.isEmpty()) {
//            passwordError.setText("Password is required");
//            isValid = false;
//        }
//        if (!password.equals(confirmPassword)) {
//            confirmPasswordError.setText("Passwords do not match");
//            isValid = false;
//        }

        if (isValid) {
            BusOwner busOwner = new BusOwner(busId, password, name, email);

            Socket socket = new Socket("localhost", 5063);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Request req=new Request("BusOwner","SignUp",busOwner.toCSV());
            oos.writeObject(req);

            Response response = (Response) ois.readObject();
            if (response.isSuccess()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BusRouteSetUp.fxml"));
                Parent root = loader.load();
                BusRouteSetUp controller = loader.getController();
                controller.setSocket(socket,ois,oos);
                controller.setBusId(busOwner.getBusId(),name);
                Stage stage = (Stage) busIdError.getScene().getWindow();
                stage.setScene(new Scene(root));
                root.getStylesheets().add(getClass().getResource("/view/BusRouteSetUp.css").toExternalForm());
            } else {
                busIdError.setText("Invalid email or password");
            }

        }
    }

    private void clearErrors() {
        busIdError.setText("");
        emailError.setText("");
        nameError.setText("");
        passwordError.setText("");
        confirmPasswordError.setText("");
        formError.setText("");
    }

    private void clearForm() {
        busIdField.clear();
        emailField.clear();
        nameField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    @FXML
    private void handleBack() {
        BackToPrevScene.navigateTo(backButton, "/view/BusOwnerLogIn.fxml","/view/BusOwnerLogIn.css");
    }
}