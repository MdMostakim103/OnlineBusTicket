package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import model.Message;
import model.Request;
import model.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class PassengerReport {
    @FXML private Button backButton;
    @FXML private Button submitButton;
    @FXML private TextArea reportTextField;
    @FXML private Button contactAcminButton;
    @FXML private AnchorPane rootPane;
    @FXML private TextField adminNameTextField;

    private String email;
    private String name;

    @FXML
    private void backToPrevScene() throws IOException {
        reportTextField.clear();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PassengerHomePage.fxml"));
        Parent root = loader.load();
        PassengerHomePage controller = loader.getController();
        controller.setPassenger(email,name);
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        String css = this.getClass().getResource("/view/PassengerHomePage.css").toExternalForm();
        root.getStylesheets().add(css);
    }

    @FXML
    private void submitReport() {
        String reportText = reportTextField.getText().trim();
        String adminName = adminNameTextField.getText().trim();
        if (reportText.isEmpty()|| adminName.isEmpty()) {
            showAlert("Error", "Please enter a valid report or admin name.");
            return;
        }
        Message messageObj = new Message("Passenger",email,"Admin",adminName,reportText);
        Request req=new Request("Passenger","ContactAdmin",messageObj.toCSV());
        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            oos.writeObject(req);
            Response response = (Response) ois.readObject();

            if (response.isSuccess()) {
                System.out.println("Message sent successfully.");
            } else {
                System.out.println("Failed to send message.");
                showAlert("Error", response.getData().toString());
                return;
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        System.out.println("Sending to " + email + ": " + reportText);
        showAlert("Message Sent", "Your message has been sent to: " + adminName);

        // Clear fields
        reportTextField.clear();
        adminNameTextField.clear();
    }


    public void setPassenger(String email,String name){
        this.email = email;
        this.name=name;
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}