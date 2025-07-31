package model;

import java.io.Serializable;
import java.util.List;

public class BusRoute implements Serializable {
    private String busId;
    private List<BusStop> stops;
    private String totalSeats;

    public BusRoute(String busId, List<BusStop> stops, String totalSeats) {
        this.busId = busId;
        this.stops = stops;
        this.totalSeats = totalSeats;
    }

    // Getters and setters
    public String getBusId() { return busId; }
    public List<BusStop> getStops() { return stops; }
    public String getTotalSeats() { return totalSeats; }

    public String toCSV() {
        String stopDetails = "";
        for(BusStop stop : stops) {
            stopDetails += stop.getStopName() + "," + stop.getArrivalTime() + "," + stop.getDepartureTime() + "," + stop.getFare() + "\n";
        }
        return busId + "," + totalSeats+"\n"+stopDetails;
    }

}