package fr.ippon.kafka.streams.serdes.pojos;

public class SoundMessage {
    private String action = "PLAY";
    private String path;

    public SoundMessage() {
    }

    public SoundMessage(String path) {
        this.path = path;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
