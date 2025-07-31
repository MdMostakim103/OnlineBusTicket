package model;

import java.io.Serializable;
import java.util.List;


public class BusOwner extends User  implements Serializable{
    private String busId;
    private List<String> routes;
    private List<Double> fares;
    private List<String> departureTimes;

    public BusOwner(String busId, String password, String name, String email) {
        super(name, email, password);
        this.busId = busId;
    }

    // Methods to add route details
    public void addRoute(String stops, double fare, String time) {
        routes.add(stops);
        fares.add(fare);
        departureTimes.add(time);
    }

    public String getBusId() { return busId; }

    public String toCSV() {
        return busId + "," + password + "," + name + "," + email;
    }

}