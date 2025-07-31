package model;

public class Notification {
    private String sender;
    private String whatSent;
    private String whenSent;

    public Notification(String whoSent, String message, String whenSent) {
        this.sender = whoSent;
        this.whatSent = message;
        this.whenSent = whenSent;
    }

    public String getWhoSent() {
        return sender;
    }

    public String getWhatSent() {
        return whatSent;
    }

    public String getWhenSent() {
        return whenSent;
    }
}