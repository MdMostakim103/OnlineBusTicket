package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Message;
import model.Request;
import model.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class AdminContactBusOwner {
    @FXML
    private Button backButton;

    @FXML
    private Button submitButton;

    @FXML
    private TextArea reportTextField;

    @FXML
    private TextField busIdTextField;

    private String name;

    public void setAdmin(String name){
        this.name=name;
    }

    @FXML
    void backToPrevScene(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/AdminHomePage.fxml"));
            Parent root = loader.load();
            AdminHomePage controller = loader.getController();
            controller.setAdmin(name);

            Stage stage = (Stage) (reportTextField).getScene().getWindow();
            stage.setScene(new Scene(root));
            root.getStylesheets().add(getClass().getResource("/view/AdminHomePage.css").toExternalForm());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to return to previous scene.");
        }
    }

    @FXML
    void submitReport(ActionEvent event) {
        String busId =busIdTextField.getText().trim();
        String message = reportTextField.getText().trim();

        if (busId.isEmpty() || message.isEmpty()) {
            showAlert("Validation Error", "Please fill in both the email and message.");
            return;
        }

        Message messageObj = new Message("Admin",name,"BusOwner",busId,message);
        Request req=new Request("Admin","ContactBusOwner",messageObj.toCSV());
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
        System.out.println("Sending to " + busId + ": " + message);
        showAlert("Message Sent", "Your message has been sent to: " + busId);

        // Clear fields
        reportTextField.clear();
        busIdTextField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

