package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class StartUpPage {
    private Stage stage;
    private Parent root;
    private Scene scene;

    @FXML
    private ImageView startUpImage;

    private void loadImage() {
        Image image = new Image(StartUpPage.class.getResourceAsStream("/Pic/StartUpScreen.png"));
        startUpImage.setImage(image);
        startUpImage.setFitHeight(600);
        startUpImage.setFitWidth(800);
        startUpImage.setPreserveRatio(false);
        startUpImage.setSmooth(true);
    }

    public void passenger(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/view/PassengerLogIn.fxml"));
        stage = (Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        String logInInterface = getClass().getResource("/view/PassengerLogIn.css").toExternalForm();
        scene.getStylesheets().add(logInInterface);
        stage.setScene(scene);
        stage.show();
    }

    public void vehicleOwner(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/view/BusOwnerLogIn.fxml"));
        stage = (Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        String logInInterface = getClass().getResource("/view/BusOwnerLogIn.css").toExternalForm();
        scene.getStylesheets().add(logInInterface);
        stage.setScene(scene);
        stage.show();
    }

    public void admin(ActionEvent event) throws IOException {
        root = FXMLLoader.load(getClass().getResource("/view/AdminLogIn.fxml"));
        stage = (Stage)((javafx.scene.Node)event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        String logInInterface = getClass().getResource("/view/AdminLogIn.css").toExternalForm();
        scene.getStylesheets().add(logInInterface);
        stage.setScene(scene);
        stage.show();
    }
}