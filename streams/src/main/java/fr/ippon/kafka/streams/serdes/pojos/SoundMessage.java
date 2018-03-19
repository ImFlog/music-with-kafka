package fr.ippon.kafka.streams.serdes.pojos;

import java.util.List;

public class SoundMessage {
    private String action = "PLAY";
    private List<String> sounds;

    public SoundMessage() {
    }

    public SoundMessage(List<String> sounds) {
        this.sounds = sounds;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public List<String> getSounds() {
        return sounds;
    }

    public void setSounds(List<String> sounds) {
        this.sounds = sounds;
    }
}
