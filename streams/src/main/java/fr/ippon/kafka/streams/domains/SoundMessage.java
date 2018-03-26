package fr.ippon.kafka.streams.domains;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class SoundMessage {

    private List<String> sounds;

    public SoundMessage(List<String> sounds) {
        this.sounds = sounds;
    }

}
