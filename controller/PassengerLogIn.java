package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.Request;
import model.Response;
import util.BackToPrevScene;
import util.Notification;
import util.Validation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PassengerLogIn {
    @FXML private TextField usernameTextfield;
    @FXML private TextField emailTextfield;
    @FXML private PasswordField passwordTextfield;
    @FXML private Label invalidLabel;
    @FXML private Label forgotPassword;
    @FXML private Label signUp;

    @FXML
    private void logIn() {
        String email = emailTextfield.getText().trim();
        String password = passwordTextfield.getText().trim();
        String name = usernameTextfield.getText().trim();

        if (!Validation.isValidEmail(email)) {
            invalidLabel.setText("Invalid email format");
            return;
        }

        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            String temp=email+","+name+","+password;
            Request request = new Request("Passenger","LogIn",temp);
            oos.writeObject(request);
            Response response=(Response) ois.readObject();
            if (response.isSuccess()) {

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PassengerHomePage.fxml"));
                Parent root = loader.load();
                PassengerHomePage controller = loader.getController();
                controller.setPassenger(email,name);
                Stage stage = (Stage) emailTextfield.getScene().getWindow();
                stage.setScene(new Scene(root));
                root.getStylesheets().add(getClass().getResource("/view/PassengerHomePage.css").toExternalForm());
            } else {
                invalidLabel.setText("Invalid email or password");
            }

        } catch (IOException | ClassNotFoundException e) {
            Notification.showError("Login Error", "Server error occurred.");
            e.printStackTrace();
        }
    }

    @FXML
    private void forgotPassword(MouseEvent event) {
        BackToPrevScene.navigateTo(emailTextfield, "/view/PassengerForgotPass.fxml","/view/PassengerForgotPass.css");
    }

    @FXML
    private void signUp(MouseEvent event) {
        BackToPrevScene.navigateTo(emailTextfield, "/view/PassengerSignup.fxml","/view/PassengerSignup.css");
    }

    @FXML
    private void backToPrevScene() {
        BackToPrevScene.navigateTo(emailTextfield, "/view/StartUpPage.fxml","/view/StartUpPage.css");
    }
}