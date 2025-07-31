package model;

import java.io.Serializable;

public class Message implements Serializable {
    private String senderRole;
    private String sender;
    private String receiver;
    private String receiverRole;
    private String message;

    public Message(String senderRole, String sender, String receiverRole, String receiver, String message) {
        this.senderRole = senderRole;
        this.sender = sender;
        this.receiver = receiver;
        this.receiverRole = receiverRole;
        this.message = message;
    }
    public String getSenderRole() { return senderRole; }
    public String getSender() { return sender; }
    public String getReceiver() { return receiver; }
    public String getReceiverRole() { return receiverRole; }
    public String getMessage() { return message; }
    public String toCSV() {
        return senderRole + "," + sender + "," + receiverRole + "," + receiver + "," + message;
    }
}
