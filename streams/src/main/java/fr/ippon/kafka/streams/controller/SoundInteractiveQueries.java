package fr.ippon.kafka.streams.controller;

import fr.ippon.kafka.streams.domains.SoundPlayCount;
import fr.ippon.kafka.streams.topologies.SoundsTopology;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
public class SoundInteractiveQueries {

    private SoundsTopology soundsTopology;

    public SoundInteractiveQueries(SoundsTopology soundsTopology) {
        this.soundsTopology = soundsTopology;
    }

    @GetMapping(value = "/tops", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Flux<SoundPlayCount> getTop() {
        return Flux.fromStream(soundsTopology.getLastTopSongs());
    }

}
