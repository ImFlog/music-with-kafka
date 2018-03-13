package fr.ippon.kafka.streams.serdes.pojos;

public class SoundMessage {
    private String action = "PLAY";
    private String name;

    public SoundMessage() {
    }

    public SoundMessage(String name) {
        this.name = name;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
