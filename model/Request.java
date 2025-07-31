package model;
import java.io.Serializable;


public class Request implements Serializable {
    private String role;
    private String action;
    private String data;

    public Request(String role,String action, String data) {
        this.action = action;
        this.data = data;
        this.role = role;
    }

    public String getAction() { return action; }
    public String getData() { return data; }
    public String getRole() { return role; }
}