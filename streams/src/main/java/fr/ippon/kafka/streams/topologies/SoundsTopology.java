package fr.ippon.kafka.streams.topologies;

import fr.ippon.kafka.streams.domains.*;
import fr.ippon.kafka.streams.serdes.SerdeException;
import fr.ippon.kafka.streams.serdes.SerdeFactory;
import fr.ippon.kafka.streams.serdes.TimedWindowedSerde;
import fr.ippon.kafka.streams.serdes.WindowedSerde;
import fr.ippon.kafka.streams.utils.Audio;
import fr.ippon.kafka.streams.utils.Commons;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static fr.ippon.kafka.streams.utils.Const.*;
import static java.util.stream.Collectors.toList;

@Component
/**
 * Send a {@link SoundMessage.class} to SOUNDS_TOPIC for each category every WINDOWING_TIME.
 */
public class SoundsTopology implements CommandLineRunner {

    private static final String APPLICATION_ID_VALUE = "SoundsTopology";
    private static final String BOOTSTRAP_SERVERS_VALUE = "localhost:9092";
    private static final String AUTO_OFFSET_VALUE = "earliest";

    // Define custom serdes
    private final Map<String, Object> serdeProps = new HashMap<>();
    private final Serde<TwitterStatus> twitterStatusSerde = SerdeFactory.createSerde(TwitterStatus.class, serdeProps);
    private final Serde<SoundPlayCount> soundPlayCountSerde = SerdeFactory.createSerde(SoundPlayCount.class, serdeProps);
    private final Serde<TopSongs> topSongSerde = SerdeFactory.createSerde(TopSongs.class, serdeProps);
    private final Serde<SoundMessage> soundMessageSerde = SerdeFactory.createSerde(SoundMessage.class, serdeProps);
    private final Serde<String> stringSerde = Serdes.String();
    private final Serde<Windowed<String>> windowedSerde = new WindowedSerde<>(stringSerde);
    final TimedWindowedSerde<String> timedWindowedSerde = new TimedWindowedSerde<>(stringSerde);

    private KafkaStreams streams;

    @Override
    public void run(String... args) {

        // Create an instance of StreamsConfig from the Properties instance
        StreamsConfig config = kStreamConfig();
        StreamsBuilder builder = new StreamsBuilder();

        //Fetch available sounds categories
        final Categories categories = Audio.retrieveAvailableCategories();


        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                "http://localhost:8081"
        );


        final KStream<String, TwitterStatus> playEvent = builder
                .stream(TWITTER_TOPIC, Consumed.with(stringSerde, twitterStatusSerde));

        final KTable<Windowed<String>, Long> songPlayCount = playEvent
                // group by categories
                .groupBy((key, value) -> Audio.findCategory(value, categories), Serialized.with(stringSerde, twitterStatusSerde))
                .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOWING_TIME)).until(TimeUnit.SECONDS.toMillis(30L))) // Tumbling windowing
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(TWEET_PER_CATEGORY)
                        .withValueSerde(Serdes.Long()));


        final KTable<Windowed<String>, TopSongs> songsKTable = songPlayCount
                .groupBy((windowedCategories, count) -> {
                            //we have to use a windowed key to run aggregation on timed windows
                            Windowed<String> windowedKey = new Windowed<>("TOPS", windowedCategories.window());
                            return KeyValue.pair(windowedKey, new SoundPlayCount(windowedCategories.key(), count));
                        },
                        Serialized.with(timedWindowedSerde, soundPlayCountSerde))
                .aggregate(TopSongs::new,
                        (key, value, aggregate) -> {
                            aggregate.add(value);
                            return aggregate;
                        },
                        (key, value, aggregate) -> {
                            aggregate.remove(value);
                            return aggregate;
                        },
                        Materialized.<Windowed<String>, TopSongs, KeyValueStore<Bytes, byte[]>>as(TOP_SONG)
                                .withValueSerde(topSongSerde));


        //We will retrieve top N songs from aggregate results
        final KTable<Windowed<String>, SoundMessage> topNSounds = songsKTable
                .mapValues(topSongs -> {
                    List<String> paths = topSongs
                            .toStream()
                            .limit(5)
                            .map(SoundPlayCount::getName)
                            .map(s -> String.format("%s/%s%d.ogg", s, s, categories.fetchRandomIndex(s)))
                            .collect(toList());
                    return new SoundMessage(paths);
                });

        topNSounds
                .toStream()
                .filter(((key, value) -> {
                    System.out.println("---------------------------------------------------");
                    long end = key.window().end();
                    System.out.println(end);
                    long now = Instant.now().toEpochMilli();
                    System.out.println(now);
                    System.out.println(now - end);
                    System.out.println(end < now);
                    System.out.println("---------------------------------------------------");
                    return true;
                }))
                .map((windowedKey, message) -> KeyValue.pair(windowedKey.toString(), message))
                .to(SOUNDS_TOPIC, Produced.with(stringSerde, soundMessageSerde));

        streams = new KafkaStreams(builder.build(), config);

        // Clean local store between runs
        streams.cleanUp();
        streams.start();


    }

    @PreDestroy
    public void destroy() {
        streams.close();
    }

    public Stream<SoundPlayCount> getLastTopSongs() {
        return Commons.iteratorToStream(getTopSongsStore().all())
                .max(Comparator.comparingLong(kv -> kv.key.window().start()))
                .map(kv -> kv.value)
                .orElseGet(TopSongs::new)
                .toStream();

    }

    public Stream<String> getTweetsPerCategories(String key) {
        return Commons.iteratorToStream(getTweetsStore().fetch(key, 0, System.currentTimeMillis()))
                .map(k -> "times = " + k.key + " - count = " + k.value);
    }

    private ReadOnlyKeyValueStore<Windowed<String>, TopSongs> getTopSongsStore() {
        return streams.store(TOP_SONG, QueryableStoreTypes.keyValueStore());
    }

    private ReadOnlyWindowStore<String, Long> getTweetsStore() {
        return streams.store(TWEET_PER_CATEGORY, QueryableStoreTypes.windowStore());
    }

    /**
     * Init stream properties.
     *
     * @return the created stream settings.
     */
    private static StreamsConfig kStreamConfig() {

        Properties settings = new Properties();
        // Application ID, used for consumer groups
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID_VALUE);
        // Kafka bootstrap server (broker to talk to)
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS_VALUE);

        // default serdes for serializing and deserializing key and value from and to streams
        settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        //ignore deserialization exception
        settings.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, SerdeException.class);

        settings.put(StreamsConfig.WINDOW_STORE_CHANGE_LOG_ADDITIONAL_RETENTION_MS_CONFIG, 30000L);
        settings.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);

        // Enable exactly once
//        settings.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        // We can also set Consumer properties
        settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_VALUE);
        return new StreamsConfig(settings);
    }

}

