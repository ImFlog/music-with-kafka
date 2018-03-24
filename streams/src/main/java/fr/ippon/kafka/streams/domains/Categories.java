package fr.ippon.kafka.streams.domains;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

public class Categories {

    private final Random random = new Random();

    private final Map<String, Integer> categoryMap = new HashMap<>();

    public Integer fetchRandomIndex(String key) {
        Integer index = categoryMap.get(key);
        return random.nextInt(index) + 1;
    }

    public void add(String key, Integer index) {
        categoryMap.put(key, index);
    }

    public Categories merge(Categories categories) {
        categoryMap.putAll(categories.categoryMap);
        return this;
    }

    public Stream<String> toStream() {
        return categoryMap.entrySet().stream().map(Map.Entry::getKey);
    }

}
