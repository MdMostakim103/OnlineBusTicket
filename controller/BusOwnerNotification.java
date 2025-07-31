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

public class BusOwnerNotification {

    @FXML private TableView<Notification> notificationTable;
    @FXML private TableColumn<Notification, String> whoSentColumn;
    @FXML private TableColumn<Notification, String> whatSentColumn;
    @FXML private TableColumn<Notification, String> whenSentColumn;
    @FXML private Button backButton;
    private String busId;
    private String busName;

    public void setBusOwner(String id,String name) throws IOException {
        this.busId=id;
        this.busName=name;
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

            Request request = new Request("BusOwner", "CheckNotification", busId);
            oos.writeObject(request);
            Response res = (Response) ois.readObject();
            if(res.isSuccess()) {
                String str=(String)res.getData();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BusOwnerHomePage.fxml"));
            Parent root = loader.load();
            BusOwnerHomePage controller = loader.getController();
            controller.setBusOwner(busId,busName);

            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            root.getStylesheets().add(getClass().getResource("/view/BusOwnerHomePage.css").toExternalForm());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
