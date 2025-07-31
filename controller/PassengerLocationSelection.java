package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Bus;
import model.Passenger;
import model.Request;
import model.Response;
import util.BackToPrevScene;
import util.Notification;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class PassengerLocationSelection {

    @FXML private ChoiceBox<String> fromChoiceBox;
    @FXML private ChoiceBox<String> toChoiceBox;
    @FXML private DatePicker journeyDatePicker;
    @FXML private Button backButton;
    @FXML private Button searchButton;
    @FXML private Label warningLabel;

    private String email;
    private String name;

    // List of all 64 districts of Bangladesh
    private final ObservableList<String> allDistricts = FXCollections.observableArrayList(
            "Bagerhat", "Bandarban", "Barguna", "Barisal", "Bhola", "Bogra", "Brahmanbaria",
            "Chandpur", "Chapai Nawabganj", "Chattogram", "Chuadanga", "Comilla", "Cox's Bazar",
            "Dhaka", "Dinajpur", "Faridpur", "Feni", "Gaibandha", "Gazipur", "Gopalganj",
            "Habiganj", "Jamalpur", "Jessore", "Jhalokati", "Jhenaidah", "Joypurhat", "Khagrachari",
            "Khulna", "Kishoreganj", "Kurigram", "Kushtia", "Lakshmipur", "Lalmonirhat", "Madaripur",
            "Magura", "Manikganj", "Meherpur", "Moulvibazar", "Munshiganj", "Mymensingh", "Naogaon",
            "Narail", "Narayanganj", "Narsingdi", "Natore", "Netrokona", "Nilphamari", "Noakhali",
            "Pabna", "Panchagarh", "Patuakhali", "Pirojpur", "Rajbari", "Rajshahi", "Rangamati",
            "Rangpur", "Satkhira", "Shariatpur", "Sherpur", "Sirajganj", "Sunamganj", "Sylhet",
            "Tangail", "Thakurgaon"
    );

    public void setPassenger(String email,String name) {
        this.email=email;
        this.name=name;
    }
    @FXML
    private void initialize() {
        LocalDate today = LocalDate.now();
        journeyDatePicker.setValue(today);

        // Set date range (today to today + 10 days)
        journeyDatePicker.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                // Disable dates before today and after today+10
                setDisable(empty ||
                        date.isBefore(today) ||
                        date.isAfter(today.plusDays(10)));

                // Optional: Highlight today's date
                if (date.equals(today)) {
                    setStyle("-fx-background-color: #b3e6ff;");
                }
            }
        });

        fromChoiceBox.getItems().addAll(allDistricts);
        toChoiceBox.getItems().addAll(allDistricts);

        fromChoiceBox.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> updateDestinationChoices(newValue));
    }

    private void updateDestinationChoices(String selectedDeparture) {
        toChoiceBox.getSelectionModel().clearSelection();
        String currentDestination = toChoiceBox.getValue();
        toChoiceBox.getItems().setAll(allDistricts);

        if (selectedDeparture != null) {
            toChoiceBox.getItems().remove(selectedDeparture);
        }
        if (currentDestination != null && toChoiceBox.getItems().contains(currentDestination)) {
            toChoiceBox.setValue(currentDestination);
        }
    }

    @FXML
    private void backToPrevScene() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PassengerHomePage.fxml"));
        Parent root = loader.load();
        PassengerHomePage controller = loader.getController();
        controller.setPassenger(email,name);
        Stage stage = (Stage) searchButton.getScene().getWindow();
        stage.setScene(new Scene(root));
        root.getStylesheets().add(getClass().getResource("/view/PassengerHomePage.css").toExternalForm());
    }

    @FXML
    private void Search() {
        String from = fromChoiceBox.getValue();
        String to = toChoiceBox.getValue();
        LocalDate date = journeyDatePicker.getValue();

        if (from == null || to == null || date == null) {
            warningLabel.setText("Please fill all fields!");
            warningLabel.setTextFill(Color.RED);
            return;
        }

        if (from.equals(to)) {
            warningLabel.setText("Departure and Destination cannot be same!");
            warningLabel.setTextFill(Color.RED);
            return;
        }

        warningLabel.setText("Searching for Buses...");
        warningLabel.setTextFill(Color.GREEN);

        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            // Send request to server
            String requestData = from + "," + to + "," + date.toString();
            Request request = new Request("Passenger", "SearchBus", requestData);
            oos.writeObject(request);
            Response response = (Response) ois.readObject();
            System.out.println(response.isSuccess());
            if (response.isSuccess()) {
                List<Bus> busList = (List<Bus>) response.getData();
                if (busList.isEmpty()) {
                    System.out.println("busList is empty");
                    warningLabel.setText("No buses found for this route!");
                    warningLabel.setTextFill(Color.RED);
                } else {
                    ObservableList<Bus> buses = FXCollections.observableArrayList(busList);
                    loadResultsScene(buses, from, to, date);
                }
            } else {
                warningLabel.setText("Search failed: " + response.getData());
                warningLabel.setTextFill(Color.RED);
            }

        } catch (IOException | ClassNotFoundException e) {
            Notification.showError("Search Error", "Server error occurred.");
            e.printStackTrace();
            warningLabel.setText("Connection failed.");
            warningLabel.setTextFill(Color.RED);
        }
    }

    private void loadResultsScene(ObservableList<Bus> buses, String from, String to, LocalDate date) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MatchedBuses.fxml"));
            Parent root = loader.load();
            MatchedBuses controller = loader.getController();
            controller.initData(buses, from, to, date,email,name);
            Scene scene = new Scene(root);
            Stage stage = (Stage) searchButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Available Buses");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            warningLabel.setText("Error loading results!");
            warningLabel.setTextFill(Color.RED);
        }
    }
}