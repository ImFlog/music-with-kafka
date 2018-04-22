package fr.ippon.kafka.streams.controller;

import fr.ippon.kafka.streams.domains.sound.SoundPlayCount;
import fr.ippon.kafka.streams.topologies.SoundsTopology;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static java.util.stream.Collectors.joining;

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

    @GetMapping(value = "/topsTweets/{key}")
    public Mono<String> getTopTweets(@PathVariable("key") String key) {
        return Flux.fromStream(soundsTopology.getTweetsPerCategories(key)).collect(joining("\n\n"));
    }

}
