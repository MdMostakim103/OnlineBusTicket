package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Passenger;
import model.Request;
import model.Response;
import util.BackToPrevScene;
import server.PassengerFileHandler;
import util.Validation;
import util.Notification;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PassengerForgotPass {
    @FXML private TextField emailTextfield;
    @FXML private Label enterEmailLabel;
    @FXML private Button verifyButton;
    @FXML private Button backButton;

    @FXML
    private void verify() throws IOException {
        String email = emailTextfield.getText().trim();

        if (email.isEmpty()) {
            Notification.showError("Email Required", "Please enter your email address");
            return;
        }

        if (!Validation.isValidEmail(email)) {
            Notification.showError("Invalid Email", "Please enter a valid email address");
            return;
        }



        try {
            Socket socket = new Socket("localhost", 5063);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            Request req=new Request("Passenger","CheckEmail",email);
            oos.writeObject(req);
            Response response=(Response) ois.readObject();
            if(!response.isSuccess()){
                Notification.showError("Invalid Email", "Please enter a valid email address");
                ois.close();
                oos.close();
                socket.close();
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PassengerResetPassword.fxml"));
            Parent root = loader.load();
            PassengerResetPassword controller = loader.getController();
            controller.setUserEmail(email);
            ois.close();
            oos.close();
            socket.close();
            Stage stage = (Stage) emailTextfield.getScene().getWindow();
            stage.setScene(new Scene(root));
            String css = this.getClass().getResource("/view/PassengerResetPassword.css").toExternalForm();
            root.getStylesheets().add(css);

        } catch (IOException e) {
            Notification.showError("Navigation Error", "Failed to load reset password page");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        Notification.showSuccess("Reset Sent",
                "you will be redirect to reset password page shortly");
        emailTextfield.clear();

    }

    @FXML
    private void backToPrevScene() throws IOException {
        BackToPrevScene.navigateTo(backButton, "/view/PassengerLogIn.fxml", "/view/PassengerLogIn.css");
    }
}