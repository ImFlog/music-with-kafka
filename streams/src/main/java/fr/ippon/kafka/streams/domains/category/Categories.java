package fr.ippon.kafka.streams.domains.category;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class Categories {

    private final Map<String, Integer> categoryMap = new HashMap<>();
    private final Map<String, Queue<Integer>> previousRandom = new HashMap<>();

    public Integer fetchRandomIndex(String key) {
        Queue<Integer> queue = previousRandom.get(key);
        Integer index = categoryMap.get(key);
        int random = getNumberWithExclusion(index, queue);
        if (!queue.contains(random)) {
            queue.add(random);
        }
        if (queue.size() >= index) {
            queue.clear();
        }
        previousRandom.put(key, queue);
        return random;
    }

    public void add(String key, Integer index) {
        categoryMap.put(key, index);
        previousRandom.put(key, new LinkedList<>());
    }

    public Categories merge(Categories categories) {
        categoryMap.putAll(categories.categoryMap);
        return this;
    }

    public Stream<String> toStream() {
        return categoryMap.entrySet().stream().map(Map.Entry::getKey);
    }

    private int getNumberWithExclusion(int size, Queue<Integer> toExclude) {
        final Random rnd = new Random();
        List<Integer> integers = IntStream
                .range(1, size + 1)
                .filter(n -> !toExclude.contains(n))
                .boxed()
                .collect(Collectors.toList());
        int bound = size - toExclude.size();
        int index = rnd.nextInt(bound);
        return integers.get(index);
    }

}
