package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import model.PassengerInfo;
import model.Request;
import model.Response;
import util.BackToPrevScene;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class AdminLogIn {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML
    private void handleLogin(ActionEvent event) {

        String info= usernameField.getText()+","+passwordField.getText();
        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            Request req=new Request("Admin","LogIn",info);
            oos.writeObject(req);
            Response res=(Response) ois.readObject();
            System.out.println(res.isSuccess());
            if(res.isSuccess()){
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminHomePage.fxml"));
                Parent root = loader.load();
                AdminHomePage controller = loader.getController();
                controller.setAdmin(usernameField.getText());
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                String css = this.getClass().getResource("/view/AdminHomePage.css").toExternalForm();
                root.getStylesheets().add(css);
            }
        } catch (ClassNotFoundException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void handleBack(ActionEvent event) {
        BackToPrevScene.navigateTo(usernameField, "/view/StartUpPage.fxml","/view/StartUpPage.css");
    }

    @FXML
    private void forgotPass(MouseEvent event) {
        BackToPrevScene.navigateTo(usernameField,"/view/AdminForgotPass.fxml","/view/AdminForgotPass.css");
    }
}
