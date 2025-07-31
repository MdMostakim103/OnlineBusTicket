package controller;

import javafx.beans.property.ReadOnlyObjectWrapper;
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
import model.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BusRouteSetUp {

    @FXML private ComboBox<String> departureComboBox;
    @FXML private TextField departureTimeField;
    @FXML private TextField totalSeatsField;

    @FXML private ComboBox<String> stopComboBox;
    @FXML private TextField arrivalTimeField;
    @FXML private TextField departureTimeStopField;
    @FXML private TextField fareTextField;
    @FXML private TextField availableSeatsField;

    @FXML private Button addStopButton;
    @FXML private Button saveButton;
    @FXML private Button clearButton;

    @FXML private TableView<BusStop> stopsTableView;
    @FXML private TableColumn<BusStop, Integer> sequenceColumn;
    @FXML private TableColumn<BusStop, String> stopNameColumn;
    @FXML private TableColumn<BusStop, String> arrivalTimeColumn;
    @FXML private TableColumn<BusStop, String> departureTimeColumn;
    @FXML private TableColumn<BusStop, Double> fareColumn;
    @FXML private TableColumn<BusStop, Void> actionColumn;

    private BusOwner busOwner;
    private String busId;
    private String busName;
    private Socket socket;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private final ObservableList<BusStop> stopList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        ObservableList<String> districts = FXCollections.observableArrayList(
                "Bagerhat", "Bandarban", "Barguna", "Barisal", "Bhola", "Bogra", "Brahmanbaria",
                "Chandpur", "Chapainawabganj", "Chattogram", "Chuadanga", "Comilla", "Cox's Bazar",
                "Dhaka", "Dinajpur", "Faridpur", "Feni", "Gaibandha", "Gazipur", "Gopalganj",
                "Habiganj", "Jamalpur", "Jashore", "Jhalokati", "Jhenaidah", "Joypurhat", "Khagrachari",
                "Khulna", "Kishoreganj", "Kurigram", "Kushtia", "Lakshmipur", "Lalmonirhat", "Madaripur",
                "Magura", "Manikganj", "Meherpur", "Moulvibazar", "Munshiganj", "Mymensingh", "Naogaon",
                "Narail", "Narayanganj", "Narsingdi", "Natore", "Netrokona", "Nilphamari", "Noakhali",
                "Pabna", "Panchagarh", "Patuakhali", "Pirojpur", "Rajbari", "Rajshahi", "Rangamati",
                "Rangpur", "Satkhira", "Shariatpur", "Sherpur", "Sirajganj", "Sunamganj", "Sylhet",
                "Tangail", "Thakurgaon"
        );

        departureComboBox.setItems(districts);
        stopComboBox.setItems(districts);

        sequenceColumn.setCellValueFactory(cellData ->
                new ReadOnlyObjectWrapper<>(stopsTableView.getItems().indexOf(cellData.getValue()) + 1));
        stopNameColumn.setCellValueFactory(new PropertyValueFactory<>("stopName"));
        arrivalTimeColumn.setCellValueFactory(new PropertyValueFactory<>("arrivalTime"));
        departureTimeColumn.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        fareColumn.setCellValueFactory(new PropertyValueFactory<>("fare"));

        addActionButtonToTable();

        stopsTableView.setItems(stopList);
    }

    public void setBusOwner(BusOwner busOwner) {
        this.busOwner = busOwner;
    }
    public void setBusId(String busId,String name){
        this.busId=busId;
        this.busName=name;
    }
    public void setSocket(Socket socket, ObjectInputStream ois, ObjectOutputStream oos){
        this.socket=socket;
        this.oos=oos;
        this.ois=ois;
    }

    @FXML
    void handleAddStop(ActionEvent event) {
        String stopName = stopComboBox.getValue();
        String arrivalTime = arrivalTimeField.getText();
        String departureTime = departureTimeStopField.getText();
        String fareStr = fareTextField.getText();

        if (stopName == null || arrivalTime.isEmpty() || departureTime.isEmpty() || fareStr.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill in all fields for the stop.");
            return;
        }

        try {
            double fare = Double.parseDouble(fareStr);
            stopList.add(new BusStop(stopName, arrivalTime, departureTime, fare));
            clearStopInputs();
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Fare must be a valid number.");
        }
    }

    @FXML
    void handleSaveRoute(ActionEvent event) {
        String departure = departureComboBox.getValue();
        String time = departureTimeField.getText();
        String totalSeats = totalSeatsField.getText();

        if (departure == null || time.isEmpty() || totalSeats.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Please fill in all main route information.");
            return;
        }

        if (stopList.size() <= 0) {
            showAlert(Alert.AlertType.ERROR, "At least one stop must be added.");
            return;
        }

        BusStop firstStop = new BusStop(departure, departureTimeField.getText(), "00:00",0.0);
        if (!stopList.get(0).getStopName().equals(departure)) {
            stopList.add(0, firstStop);
        }

        BusRoute busRoute = new BusRoute(busId, stopList, totalSeats);
        String routeDetails = busRoute.toCSV();
        System.out.println(routeDetails);


        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            Request request = new Request("BusOwner", "SaveBusRoute", routeDetails);
            oos.writeObject(request);
            Response response = (Response) ois.readObject();
            System.out.println(response.isSuccess());

            if (response.isSuccess()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BusOwnerHomePage.fxml"));
                Parent root = loader.load();
                BusOwnerHomePage controller = loader.getController();
                controller.setBusOwner(busId, busName);
                Scene scene = new Scene(root);
                scene.getStylesheets().add(getClass().getResource("/view/BusOwnerHomePage.css").toExternalForm());

                Stage stage = (Stage) saveButton.getScene().getWindow();
                stage.setScene(scene);
                stage.show();

                showAlert(Alert.AlertType.INFORMATION, "Route saved successfully!");
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleClearAll(ActionEvent event) {
        departureComboBox.setValue(null);
        departureTimeField.clear();
        totalSeatsField.clear();
        stopList.clear();
        clearStopInputs();
    }

    @FXML
    void handleBack() throws IOException, ClassNotFoundException {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Route will not be saved. Are you sure you want to go back?");
        ButtonType result = alert.showAndWait().orElse(ButtonType.CANCEL);

        if (result == ButtonType.OK) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/BusOwnerHomePage.fxml"));
            Parent root = loader.load();
            BusOwnerHomePage controller = loader.getController();
            controller.setBusOwner(busId, busName);
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/view/BusOwnerHomePage.css").toExternalForm());
            Stage stage = (Stage) saveButton.getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        }
    }

    private void clearStopInputs() {
        stopComboBox.setValue(null);
        arrivalTimeField.clear();
        departureTimeStopField.clear();
        fareTextField.clear();
    }

    private void addActionButtonToTable() {
        actionColumn.setCellFactory(col -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");

            {
                removeButton.setOnAction(e -> {
                    BusStop stop = getTableView().getItems().get(getIndex());
                    stopList.remove(stop);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
    }

    private void showAlert(Alert.AlertType type, String msg) {
        Alert alert = new Alert(type);
        alert.setContentText(msg);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
