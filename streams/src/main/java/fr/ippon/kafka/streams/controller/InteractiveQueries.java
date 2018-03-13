package fr.ippon.kafka.streams.controller;

import fr.ippon.kafka.streams.processor.StreamProcessor;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

// TODO : Example only. Rewrite this is needed.
@RestController
public class InteractiveQueries {

    private StreamProcessor processor;

    public InteractiveQueries(StreamProcessor processor) {
        this.processor = processor;
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
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }

    @RequestMapping(value = "/hashtags")
    public Map<String, Set<String>> getHashtagPerUser(@RequestParam(required = false) Integer count) {
        if (count == null) {
            count = Integer.MAX_VALUE;
        }

        Map<String, Set<String>> hashTagPerUserMap = new HashMap<>();
        KeyValueIterator<String, Set<String>> hashtagPerUser = processor.getHashtagPerUser().all();
        while (hashTagPerUserMap.size() < count && hashtagPerUser.hasNext()) {
            KeyValue<String, Set<String>> next = hashtagPerUser.next();
            hashTagPerUserMap.put(next.key, next.value);
        }
        hashtagPerUser.close();

        return hashTagPerUserMap.entrySet().stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue(Comparator.comparingInt(Set::size))))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));
    }
}
