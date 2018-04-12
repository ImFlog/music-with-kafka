package fr.ippon.kafka.streams.topologies;

import fr.ippon.kafka.streams.domains.Categories;
import fr.ippon.kafka.streams.domains.SoundMessage;
import fr.ippon.kafka.streams.domains.TwitterStatus;
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
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class SoundsProcessor implements Processor<String, TwitterStatus> {

    private static final int INTERVAL = 30000;
    private static final String TOPS = "TOPS";
    private static final int MAX_SIZE = 5;
    private boolean isLaunched = false;
    private ProcessorContext context;
    private KeyValueStore<String, Long> kvStore;
    private final Categories categories = Audio.retrieveAvailableCategories();

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        kvStore = (KeyValueStore) context.getStateStore(TOP_SONG);
    }

    @Override
    public void process(String key, TwitterStatus twitterStatus) {
        if (!isLaunched) {
            isLaunched = true;
            System.out.println("Schedule Task !");
            this.context.schedule(INTERVAL, PunctuationType.WALL_CLOCK_TIME, getPunctuator());
        }

        final String category = Audio.findCategory(twitterStatus, categories);
        System.out.println("Adding " + category + " to state store");
        final Long count = this.kvStore.get(category);
        if (null != count) {
            this.kvStore.put(category, count + 1L);
        } else {
            this.kvStore.put(category, 1L);
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

    private Punctuator getPunctuator() {
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
