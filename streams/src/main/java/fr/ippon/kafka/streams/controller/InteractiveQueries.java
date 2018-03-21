package fr.ippon.kafka.streams.controller;

import fr.ippon.kafka.streams.processor.SoundsTopology;
import fr.ippon.kafka.streams.processor.UsersTopology;
import fr.ippon.kafka.streams.serdes.pojos.SoundPlayCount;
import fr.ippon.kafka.streams.serdes.pojos.TopSongs;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toMap;

@RestController
public class InteractiveQueries {

    private UsersTopology processor;
    private SoundsTopology soundsTopology;


    public InteractiveQueries(UsersTopology processor, SoundsTopology soundsTopology) {
        this.processor = processor;
        this.soundsTopology = soundsTopology;
    }

    @GetMapping(value = "/tops", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public Map<String, Long> getTop() {
        long max = Long.MIN_VALUE;
        Optional<TopSongs> selected = Optional.empty();
        KeyValueIterator<Windowed<String>, TopSongs> topsongs = soundsTopology.getTopSongs().all();
        while (topsongs.hasNext()) {
            KeyValue<Windowed<String>, TopSongs> next = topsongs.next();
            selected = Optional.of(next.value);
            if (next.key.window().start() > max) {
                max = next.key.window().start();
                selected = Optional.of(next.value);
            }
        }
        topsongs.close();
        return selected
                .map(topsong -> StreamSupport
                        .stream(topsong.spliterator(), false)
                        .collect(toMap(SoundPlayCount::getName, SoundPlayCount::getCount))
                )
                .orElse(Collections.emptyMap());
    }

    @RequestMapping(value = "/tweets")
    public Map<String, Long> getTweetCountPerUser(@RequestParam(required = false) Integer count) {
        if (count == null) {
            count = Integer.MAX_VALUE;
        }

        Map<String, Long> tweetCountPerUser = new HashMap<>();
        KeyValueIterator<String, Long> tweetCounts = processor.getTweetCountPerUser().all();
        while (tweetCountPerUser.size() < count && tweetCounts.hasNext()) {
            KeyValue<String, Long> next = tweetCounts.next();
            tweetCountPerUser.put(next.key, next.value);
        }
        tweetCounts.close();

        return tweetCountPerUser.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }
}
