package model;

import java.io.Serializable;

public class PassengerInfo implements Serializable {
    private String email;
    private String route;
    private String seatNumber;
    private String fare;
    private String date;

    public PassengerInfo(String email, String route, String seatNumber, String fare, String date) {
        this.email = email;
        this.route = route;
        this.seatNumber = seatNumber;
        this.fare = fare;
        this.date = date;
    }

    public String getEmail() {
        return email;
    }

    public String getRoute() {
        return route;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public String getFare() {
        return fare;
    }

    public String getDate() {
        return date;
    }
    public String toCSV() {
        return email + "," + route + "," + seatNumber + "," + fare + "," + date;
    }
}
