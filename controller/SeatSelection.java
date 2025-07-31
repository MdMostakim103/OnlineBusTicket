package controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Bus;
import model.Request;
import model.Response;
import util.Notification;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.*;
        import java.util.stream.Collectors;

public class SeatSelection {

    @FXML
    private GridPane seatGrid;

    @FXML
    private Label seatInfoLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Button backButton;

    private List<Integer> seats; // available seats
    private String from;
    private String to;
    private String date;
    private String email;
    private String name;
    private String busId;
    private double farePerSeat;
    private ObservableList<Bus> buses;

    private final Set<Integer> selectedSeats = new HashSet<>();
    private List<Integer> seatList=new ArrayList<>();

    public void init(List<Integer> seats, String from, String to, String date, String email, String name, String busId, double farePerSeat,ObservableList<Bus> buses) {
        this.seats = seats;
        this.from = from;
        this.to = to;
        this.date = date;
        this.email = email;
        this.name = name;
        this.busId = busId;
        this.farePerSeat = farePerSeat;
        this.buses=buses;

        drawSeatLayout();
        updateSeatInfo();
    }

    private void drawSeatLayout() {
        seatGrid.getChildren().clear();
        int row = 0;
        int col = 0;

        for (int seatNumber = 1; seatNumber <= 40; seatNumber++) {
            ToggleButton seatBtn = new ToggleButton(String.valueOf(seatNumber));
            seatBtn.setMinSize(45, 35);

            if (seats.contains(seatNumber)) {
                seatBtn.getStyleClass().add("available");
                int finalSeat = seatNumber;
                seatBtn.setOnAction(e -> {
                    if (seatBtn.isSelected()) {
                        selectedSeats.add(finalSeat);
                        seatBtn.getStyleClass().add("selected");
                    } else {
                        selectedSeats.remove(finalSeat);
                        seatBtn.getStyleClass().remove("selected");
                    }
                    updateSeatInfo();
                });
            } else {
                seatBtn.setDisable(true);
                seatBtn.getStyleClass().add("unavailable");
            }

            seatGrid.add(seatBtn, col, row);

            col++;
            if (col == 2) col++; // aisle
            if (col == 5) {
                col = 0;
                row++;
            }
        }
    }

    private void updateSeatInfo() {
        int count = selectedSeats.size();
        double totalFare = count * farePerSeat;
        seatInfoLabel.setText("Selected Seats: " + count + " | Total Fare: " + totalFare + " BDT");
    }

    public void handleConfirm(ActionEvent event) {
        List<Integer> seats = new ArrayList<>(selectedSeats);  // correct list of selected seats
        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            String seatListStr = seats.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining("-"));

            String str = busId + "," + email + "," + from + "-" + to + "," +
                    seatListStr + "," + (seats.size() * farePerSeat) + "," + date;

            Request request = new Request("Passenger", "ConfirmBooking", str);
            oos.writeObject(request);
            Response response = (Response) ois.readObject();

            System.out.println(response.isSuccess());
            if (response.isSuccess()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Booking Confirmed");
                alert.setHeaderText(null);
                alert.setContentText("Your seat booking has been successfully confirmed!");
                alert.showAndWait();

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PassengerHomePage.fxml"));
                Parent root = loader.load();
                PassengerHomePage controller = loader.getController();
                controller.setPassenger(email, name);

                Scene scene = new Scene(root);
                Stage stage = (Stage) confirmButton.getScene().getWindow();
                stage.setScene(scene);
                root.getStylesheets().add(getClass().getResource("/view/PassengerHomePage.css").toExternalForm());
            }

        } catch (IOException | ClassNotFoundException e) {
            Notification.showError("Booking Error", "Server error occurred.");
            e.printStackTrace();
        }
    }
    public void backToPrevScene(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/MatchedBuses.fxml"));
        Parent root = loader.load();

        MatchedBuses controller = loader.getController();
        controller.initData(buses, from, to, LocalDate.parse(date), email, name);
        Scene scene = new Scene(root);
        scene.getStylesheets().add("view/MatchedBuses.css");
        Stage stage = (Stage) backButton.getScene().getWindow();
        stage.setScene(scene);
    }

    // You’ll handle confirmButton and backButton actions in FXML or MainController
}