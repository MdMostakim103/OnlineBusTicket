package controller;


import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import model.PassengerInfo;
import model.Request;
import model.Response;
import util.BackToPrevScene;
import util.Notification;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.util.List;

public class BusOwnerHomePage {

    @FXML private TableView<PassengerInfo> todaysPassengerTable;
    @FXML private TableColumn<PassengerInfo, String> emailIdColumn;
    @FXML private TableColumn<PassengerInfo, String> routeColumn;
    @FXML private TableColumn<PassengerInfo, String> seatNumberColumn;
    @FXML private TableColumn<PassengerInfo, String> fareColumn;
    @FXML private TableColumn<PassengerInfo, String> bookingDateColumn;
    @FXML private Button logoutButton;
    private Timeline refreshTimeline;

    private LocalDate today = LocalDate.now();
    private final String todayStr = today.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    private String busId;
    private String busName;


    @FXML
    public void initialize() {
        emailIdColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("route"));
        seatNumberColumn.setCellValueFactory(new PropertyValueFactory<>("seatNumber"));
        fareColumn.setCellValueFactory(new PropertyValueFactory<>("fare"));
        bookingDateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        refreshTimeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            try {
                loadTodayPassengers();
            } catch (IOException | ClassNotFoundException ex) {
                System.err.println("Error refreshing passenger list: " + ex.getMessage());
            }
        }));
        refreshTimeline.setCycleCount(Timeline.INDEFINITE); // run forever
        refreshTimeline.play();
    }
    @FXML
    private void handleQuitButton(ActionEvent event) {
        Notification.showError("Confirmation", "Sure you want to Delete this BusOwner Account ?");

        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            Request req = new Request("BusOwner", "Delete Account", busId);
            oos.writeObject(req);
            Response res = (Response) ois.readObject();
            if (res.isSuccess()) {
                Platform.runLater(() ->
                        BackToPrevScene.navigateTo(logoutButton, "/view/BusOwnerLogin.fxml", "/view/BusOwnerLogin.css"));
            } else {
                Notification.showError("Error", "Failed to delete account");
            }

    } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

        @FXML void handleNotification(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BusOwnerNotification.fxml"));
            Parent root = loader.load();
            BusOwnerNotification controller = loader.getController();
            controller.setBusOwner(busId,busName);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            String css = this.getClass().getResource("/view/BusOwnerNotification.css").toExternalForm();
            root.getStylesheets().add(css);

        } catch (IOException e) {
            Notification.showError("Navigation Error", "Failed to load report page");
            e.printStackTrace();
        }
        // You can update this logic to load actual notifications from the server
    }

    private void loadTodayPassengers() throws IOException, ClassNotFoundException {
        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            Request request = new Request("BusOwner", "loadTodayPassengers", busId);
            oos.writeObject(request);
            Response res = (Response) ois.readObject();
            List<PassengerInfo> passengerList = (List<PassengerInfo>)res.getData();
            ObservableList<PassengerInfo> observableList = FXCollections.observableArrayList(passengerList);
            Platform.runLater(() -> todaysPassengerTable.setItems(observableList));
        }
    }

    @FXML
    void handleLogout(ActionEvent event) {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        try {
            try (Socket socket = new Socket("localhost", 5063);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

                Request req = new Request("BusOwner", "LogOut", busId);
                oos.writeObject(req);
                Response res = (Response) ois.readObject();

                if (res.isSuccess()) {
                    Platform.runLater(() ->
                            BackToPrevScene.navigateTo(logoutButton, "/view/BusOwnerLogin.fxml", "/view/BusOwnerLogin.css"));
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            Platform.runLater(() ->
                    showAlert(Alert.AlertType.ERROR, "Logout Error", "Failed to logout: " + e.getMessage()));
            e.printStackTrace();
        }
    }

    @FXML
    void ReportHandling(ActionEvent event) {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        showAlert(Alert.AlertType.INFORMATION, "Report", "Report issue window openning.");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BusOwnerReport.fxml"));
            Parent root = loader.load();
            BusOwnerReport controller = loader.getController();
            controller.setBusOwner(busId,busName);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            String css = this.getClass().getResource("/view/BusOwnerReport.css").toExternalForm();
            root.getStylesheets().add(css);

        } catch (IOException e) {
            Notification.showError("Navigation Error", "Failed to load report page");
            e.printStackTrace();
        }

    }

    @FXML
    void handleUpdateRoute(ActionEvent event) throws IOException {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
            try (Socket socket = new Socket("localhost", 5063);
                 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BusRouteSetUp.fxml"));
            Parent root=loader.load();
            BusRouteSetUp controller=loader.getController();
            controller.setBusId(busId,busName);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            root.getStylesheets().add(getClass().getResource("/view/BusRouteSetUp.css").toExternalForm());
        }
    }

    @FXML
    void handleUpdateInfo(ActionEvent event) throws IOException {
        if (refreshTimeline != null) {
            refreshTimeline.stop();
        }
        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BusOwnerUpdateInfo.fxml"));
            Parent root = loader.load();
            BusOwnerUpdateInfo controller = loader.getController();
            controller.setBusOwner(busId, busName);
            Stage stage = (Stage) logoutButton.getScene().getWindow();
            stage.setScene(new Scene(root));
            root.getStylesheets().add(getClass().getResource("/view/BusOwnerUpdateInfo.css").toExternalForm());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setBusOwner(String busId, String name) throws IOException, ClassNotFoundException {
        this.busId = busId;
        this.busName = name;
        loadTodayPassengers();
    }
}
