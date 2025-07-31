package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Notification;
import model.Request;
import model.Response;

import java.io.*;
import java.net.Socket;

public class PassengerNotification {

    @FXML private TableView<Notification> notificationTable;
    @FXML private TableColumn<Notification, String> whoSentColumn;
    @FXML private TableColumn<Notification, String> whatSentColumn;
    @FXML private TableColumn<Notification, String> whenSentColumn;
    @FXML private Button backButton;
    private String name;
    private String email;

    public void setPassenger(String email,String name) throws IOException {
        this.name=name;
        this.email=email;
        loadNotifications();
    }
    @FXML
    public void initialize() {
        whoSentColumn.setCellValueFactory(new PropertyValueFactory<>("whoSent"));
        whatSentColumn.setCellValueFactory(new PropertyValueFactory<>("whatSent"));
        whenSentColumn.setCellValueFactory(new PropertyValueFactory<>("whenSent"));
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            try {
                loadNotifications();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE); // Repeat forever
        timeline.play();
    }

    private void loadNotifications() throws IOException {
        ObservableList<Notification> list = FXCollections.observableArrayList();
        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            Request request = new Request("Passenger", "CheckNotification", email);
            oos.writeObject(request);
            Response res = (Response) ois.readObject();
            System.out.println(res.getData().toString());
            if(res.isSuccess()) {
                String str=(String)res.getData();
                if(str.isEmpty()) {
                    Platform.runLater(() -> notificationTable.setItems(list));
                    return;
                }
                String[] arr=str.split("//");
                for(int i=0;i<arr.length;i++) {
                    String[] arr1=arr[i].split(",");
                    if(arr1.length==3) {
                        list.add(new Notification(arr1[0],arr1[1],arr1[2]));
                    }
                }
                Platform.runLater(() -> {
                    notificationTable.getItems().clear();
                    notificationTable.setItems(list);
                });
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PassengerHomePage.fxml"));
            Parent root = loader.load();
            PassengerHomePage controller = loader.getController();
            controller.setPassenger(email,name);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            root.getStylesheets().add(getClass().getResource("/view/PassengerHomePage.css").toExternalForm());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
