package fr.ippon.kafka.streams.controller;

import fr.ippon.kafka.streams.processor.ChartsTopology;
import fr.ippon.kafka.streams.processor.SoundsTopology;
import fr.ippon.kafka.streams.processor.UsersTopology;
import fr.ippon.kafka.streams.serdes.pojos.*;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.toMap;

@RestController
public class InteractiveQueries {

    private UsersTopology processor;
    private SoundsTopology soundsTopology;
    private ChartsTopology chartsTopology;

    public InteractiveQueries(UsersTopology processor, SoundsTopology soundsTopology, ChartsTopology chartsTopology) {
        this.processor = processor;
        this.soundsTopology = soundsTopology;
        this.chartsTopology = chartsTopology;
    }

    @GetMapping(value = "/charts/{category}")
    public Map<String, Long> getCountByCategories(@PathVariable("category") String category) {
        WindowStoreIterator<Long> it = chartsTopology.chartsStore().fetch(category  , 0, System.currentTimeMillis());
        Map<String, Long> result = new HashMap<>();
        while (it.hasNext()) {
            KeyValue<Long, Long> keyValue = it.next();
            String date = LocalDateTime.ofInstant(Instant.ofEpochMilli(keyValue.key), ZoneId.systemDefault()).toString();
            result.put(date, keyValue.value);
        }
        it.close();
        return result;
    }

    @GetMapping(value = "/charts")
    public Map<String, Long> getLastChart() {
        long max = Long.MIN_VALUE;
        Optional<Charts> selected = Optional.empty();
        KeyValueIterator<Windowed<String>, Charts> iterator = chartsTopology.chartsStoreWindowed().all();
        while (iterator.hasNext()) {
            KeyValue<Windowed<String>, Charts> next = iterator.next();
            selected = Optional.of(next.value);
            if (next.key.window().start() > max) {
                max = next.key.window().start();
                selected = Optional.of(next.value);
            }
        }
        iterator.close();
        return selected
                .map(charts -> charts.toStream()
                        .collect(toMap(Chart::getSound, Chart::getCount))
                )
                .orElse(Collections.emptyMap());
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


    @GetMapping(value = "/users")
    public List<TwitterUserInfo> users() {
        KeyValueIterator<String, TwitterUserInfo> all = processor.getUserFeedStore().all();
        List<TwitterUserInfo> result = new ArrayList<>();
        while (all.hasNext()) {
            KeyValue<String, TwitterUserInfo> keyValue = all.next();
            result.add(keyValue.value);
        }
        return result;
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
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
