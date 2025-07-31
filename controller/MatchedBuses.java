package controller;


import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import model.Bus;
import model.Request;
import model.Response;
import util.Notification;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MatchedBuses implements Initializable {
    @FXML private Label routeLabel;
    @FXML private Label dateLabel;
    @FXML private TableView<Bus> busesTable;
    @FXML private TableColumn<Bus, String> busIdCol;
    @FXML private TableColumn<Bus, String> busNameCol;
    @FXML private TableColumn<Bus, String> departureTimeCol;
    @FXML private TableColumn<Bus, Integer> seatsCol;
    @FXML private TableColumn<Bus, Double> fareCol;
    @FXML private TableColumn<Bus, Void> actionCol;
    private String name;
    private String email;
    private String date;
    private String from;
    private String to;
    private ObservableList<Bus> buses;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Set up table columns
        busIdCol.setCellValueFactory(new PropertyValueFactory<>("busId"));
        busNameCol.setCellValueFactory(new PropertyValueFactory<>("busName"));
        departureTimeCol.setCellValueFactory(new PropertyValueFactory<>("departureTime"));
        seatsCol.setCellValueFactory(new PropertyValueFactory<>("availableSeats"));
        fareCol.setCellValueFactory(new PropertyValueFactory<>("fare"));

        // Format departure time
        departureTimeCol.setCellFactory(column -> new TableCell<Bus, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    try {
                        // Parse string to LocalTime, assuming 24-hour format "HH:mm"
                        LocalTime time = LocalTime.parse(item);
                        setText(time.format(DateTimeFormatter.ofPattern("hh:mm a"))); // e.g. 02:30 PM
                    } catch (Exception e) {
                        // In case parsing fails, show raw string
                        setText(item);
                    }
                }
            }
        });

        // Format fare
        fareCol.setCellFactory(column -> new TableCell<Bus, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("৳%.2f", item));
                }
            }
        });

        // Add book button to action column
        actionCol.setCellFactory(param -> new TableCell<Bus, Void>() {
            private final Button bookButton = new Button("Book");

            {
                bookButton.setOnAction(event -> {
                    Bus bus = getTableView().getItems().get(getIndex());
                    handleBookBus(bus);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(bookButton);
                }
            }
        });
    }

    public void initData(ObservableList<Bus> buses, String from, String to, LocalDate date,String email,String name) {
        routeLabel.setText(from + " → " + to);
        dateLabel.setText("Date: " + date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        busesTable.setItems(buses);
        this.email = email;
        this.name = name;
        this.date = date.toString();
        this.from=from;
        this.to=to;
        this.buses=buses;
    }

    private void handleBookBus(Bus bus) {
        String data = bus.getBusId() + "," + date + "," + from + "," + to;

        try (Socket socket = new Socket("localhost", 5063);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            Request request = new Request("Passenger", "ChooseSeat", data);
            oos.writeObject(request);
            Response response = (Response) ois.readObject();
            System.out.println(response.isSuccess());
            System.out.println("this is executed");
            String seats=response.getData().toString();
            LoadNextPage(seats,email,name,bus);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void LoadNextPage(String seats, String email, String name,Bus bus) throws IOException {
        // Remove brackets and whitespace from string, then convert to list of integers
        seats = seats.replace("[", "").replace("]", "").replaceAll("\\s+", "");

        List<Integer> list = Arrays.stream(seats.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        System.out.println(list);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/SeatSelection.fxml"));
            Parent root = loader.load();
            SeatSelection controller = loader.getController();
            controller.init(list, from, to, date,email,name,bus.getBusId(),bus.getFare(),buses);
            Scene scene = new Scene(root);
            Stage stage = (Stage) routeLabel.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Available Buses");
            root.getStylesheets().add(getClass().getResource("/view/SeatSelection.css").toExternalForm());
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @FXML
    private void handleBack() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/PassengerLocationSelection.fxml"));
        Parent root = loader.load();
        PassengerLocationSelection controller = loader.getController();
        controller.setPassenger(email,name);
        Stage stage = (Stage) routeLabel.getScene().getWindow();
        stage.setScene(new Scene(root));
        root.getStylesheets().add(getClass().getResource("/view/PassengerLocationSelection.css").toExternalForm());
    }
}
