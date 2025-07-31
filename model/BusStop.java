package model;

import java.io.Serializable;

public class BusStop implements Serializable {
    private String stopName;
    private String arrivalTime;
    private String departureTime;
    private double fare;

    public BusStop(String name, String arrivalTime, String departureTime, double fare) {
        this.stopName = name;
        this.arrivalTime = arrivalTime;
        this.departureTime = departureTime;
        this.fare = fare;
    }

    // Getters and setters
    public String getStopName() { return stopName; }
    public String getArrivalTime() { return arrivalTime; }
    public String getDepartureTime() { return departureTime; }
    public double getFare() { return fare; }
}