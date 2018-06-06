package ru.track.prefork;

public class Message {
    private String data;
    private long time;
    private String senderName;

    public Message(String data, long time, String senderName) {
        this.data = data;
        this.senderName = senderName;
        this.time = time;
    }

    public String getData() {
        return data;
    }

    public long getTime() {
        return time;
    }

    public String getSenderName() {
        return senderName;
    }
}
