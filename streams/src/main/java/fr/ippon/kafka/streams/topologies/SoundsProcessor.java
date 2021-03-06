package fr.ippon.kafka.streams.topologies;

import fr.ippon.kafka.streams.domains.category.Categories;
import fr.ippon.kafka.streams.domains.sound.SoundMessage;
import fr.ippon.kafka.streams.domains.twitter.TwitterStatus;
import fr.ippon.kafka.streams.utils.Audio;
import fr.ippon.kafka.streams.utils.Commons;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.processor.Punctuator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Comparator;
import java.util.function.Function;

import static fr.ippon.kafka.streams.utils.Const.TOP_SONG;
import static fr.ippon.kafka.streams.utils.Const.WINDOWING_TIME;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class SoundsProcessor implements Processor<String, TwitterStatus> {

    private static final String TOPS = "TOPS";
    private static final int MAX_SIZE = 5;
    private final Categories categories = Audio.retrieveAvailableCategories();
    private ProcessorContext context;
    private KeyValueStore<String, Long> kvStore;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        this.kvStore = (KeyValueStore) context.getStateStore(TOP_SONG);
        context.schedule(WINDOWING_TIME, PunctuationType.WALL_CLOCK_TIME, getPunctuator());
    }

    @Override
    public void process(String key, TwitterStatus twitterStatus) {
        final String category = Audio.findCategory(twitterStatus, categories);
        if (!Audio.UNKNOW.equalsIgnoreCase(category)) {
            updateStateStore(category);
        }
    }

    @Override
    public void punctuate(long timestamp) {
        // deprecated
    }

    @Override
    public void close() {
        //release
    }

    private void updateStateStore(String category) {
        System.out.println("Adding " + category + " to state store");
        final Long count = this.kvStore.get(category);
        if (null != count) {
            this.kvStore.put(category, count + 1L);
        } else {
            this.kvStore.put(category, 1L);
        }
    }

    private Punctuator getPunctuator() {
        System.out.println("Punctuator for sound");
        Comparator<KeyValue<String, Long>> comparator = Comparator.comparingLong(kv -> kv.value);
        Function<KeyValueStore<String, Long>, SoundMessage> messageGenerator = soundMessageGenerator(comparator);
        return timestamp -> {
            SoundMessage message = messageGenerator.apply(this.kvStore);
            clean(this.kvStore);
            if (message.getSounds().isEmpty()) {
                System.out.println("Nothing to do, store is empty");
            } else {
                System.out.println("forward message");
                context.forward(TOPS, message);
                context.commit();
            }
        };
    }

    private Function<KeyValueStore<String, Long>, SoundMessage> soundMessageGenerator(Comparator<KeyValue<String, Long>> comparator) {
        return store -> Commons
                .iteratorToStream(store.all())
                .sorted(comparator.reversed())
                .limit(MAX_SIZE)
                .map(kv -> kv.key)
                .map(s -> String.format("%s/%s%d.ogg", s, s, categories.fetchRandomIndex(s)))
                .collect(collectingAndThen(toList(), SoundMessage::new));
    }

    private void clean(KeyValueStore<String, Long> store) {
        Commons
                .iteratorToStream(store.all())
                .forEach(kv -> store.delete(kv.key));
    }


}
