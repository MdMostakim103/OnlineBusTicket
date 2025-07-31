package model;

import java.io.Serializable;

public class Passenger extends User implements Serializable {

    public Passenger(String email, String name, String password) {
        super(name, email, password);
    }

    // Getters
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getPassword() { return password; }

    // For file storage
    public String toCSV() {
        return email + "," + name + "," + password;
    }
}