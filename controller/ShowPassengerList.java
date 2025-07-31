package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import model.PassengerInfo;
import model.Request;
import model.Response;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ShowPassengerList {

    @FXML private TableView<PassengerInfo> passengerTable;
    @FXML private TableColumn<PassengerInfo, String> emailColumn;
    @FXML private TableColumn<PassengerInfo, String> routeColumn;
    @FXML private TableColumn<PassengerInfo, String> seatsColumn;
    @FXML private TableColumn<PassengerInfo, String> fareColumn;
    @FXML private TableColumn<PassengerInfo, String> dateColumn;
    @FXML private TableColumn<PassengerInfo, Void> contactColumn;
    @FXML private Button backButton;

    private String busId;
    private String name;

    // Dummy data source; replace with actual file/network call
    private ObservableList<PassengerInfo> passengerList = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws IOException {
        emailColumn.setCellValueFactory(new PropertyValueFactory<>("email"));
        routeColumn.setCellValueFactory(new PropertyValueFactory<>("route"));
        seatsColumn.setCellValueFactory(new PropertyValueFactory<>("seats"));
        fareColumn.setCellValueFactory(new PropertyValueFactory<>("fare"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        addContactButtonToTable();

        refreshPassengerList();
    }

    private void addContactButtonToTable() {
        contactColumn.setCellFactory(param -> new TableCell<>() {
            private final Button contactBtn = new Button("Contact");

            {
                contactBtn.setOnAction(event -> {
                    PassengerInfo passenger = getTableView().getItems().get(getIndex());
                    showContactAlert(passenger.getEmail());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(contactBtn);
                }
            }
        });
    }

    private void showContactAlert(String email) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Contact Passenger");
        alert.setHeaderText(null);
        alert.setContentText("Contact Email: " + email);
        alert.showAndWait();
    }

    private void refreshPassengerList() throws IOException {
        passengerList.clear();

        // Sample data for now
        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {
            Request req=new Request("BusOwner","LoadUpcomingPassengers",busId);
            oos.writeObject(req);
            Response res=(Response) ois.readObject();
            System.out.println(res.isSuccess());
            List<PassengerInfo> passengers = (List<PassengerInfo>) res.getData();
            passengerList.setAll(passengers);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        passengerTable.setItems(passengerList);
    }

    @FXML
    private void handleBack() throws IOException, ClassNotFoundException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BusOwnerHomePage.fxml"));
        Parent root = loader.load();
        BusOwnerHomePage controller = loader.getController();
        controller.setBusOwner(busId, name);
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        root.getStylesheets().add(getClass().getResource("/view/BusOwnerHomePage.css").toExternalForm());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Error");
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void setBusOwner(String busId, String busName) {
        this.busId=busId;
        this.name=busName;
    }
}
