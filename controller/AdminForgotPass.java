package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
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

public class AdminForgotPass {
    @FXML private TextField prevUsernameField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label promptLabel;

    @FXML
    void handleReset(ActionEvent event) {
        if(!newPasswordField.getText().equals(confirmPasswordField.getText())||newPasswordField.getText().isEmpty()){
            promptLabel.setText("Password can't be null or mismatch");
            return;
        }

        String info= prevUsernameField.getText()+","+newPasswordField.getText();
        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            Request req=new Request("Admin","ForgotPassword",info);
            oos.writeObject(req);
            Response res=(Response) ois.readObject();
            if(res.isSuccess()){
                BackToPrevScene.navigateTo(promptLabel, "/view/AdminLogIn.fxml", "/view/AdminLogIn.css");
            }
            else {
                promptLabel.setText("Wrong name");
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void handleBack(ActionEvent event) {
        BackToPrevScene.navigateTo(promptLabel, "/view/AdminLogIn.fxml", "/view/AdminLogIn.css");
    }

}
