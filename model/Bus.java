package model;

import java.io.Serializable;
import java.time.LocalTime;

public class Bus implements Serializable {
    private String busId;
    private String busName;
    private String departureTime;
    private int availableSeats;
    private double fare;

    public Bus(String busId, String busName, String departureTime, int availableSeats, double fare) {
        this.busId = busId;
        this.busName = busName;
        this.departureTime = departureTime;
        this.availableSeats = availableSeats;
        this.fare = fare;
    }

    // Getters and Setters
    public String getBusId() { return busId; }
    public String getBusName() { return busName; }
    public String getDepartureTime() { return departureTime; }
    public int getAvailableSeats() { return availableSeats; }
    public double getFare() { return fare; }

    // You can add setters if needed
}