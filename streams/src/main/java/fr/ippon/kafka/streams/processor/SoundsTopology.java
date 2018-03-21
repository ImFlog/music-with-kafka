package fr.ippon.kafka.streams.processor;

import fr.ippon.kafka.streams.serdes.SerdeException;
import fr.ippon.kafka.streams.serdes.SerdeFactory;
import fr.ippon.kafka.streams.serdes.WindowedSerde;
import fr.ippon.kafka.streams.serdes.pojos.SoundMessage;
import fr.ippon.kafka.streams.serdes.pojos.SoundPlayCount;
import fr.ippon.kafka.streams.serdes.pojos.TopSongs;
import fr.ippon.kafka.streams.serdes.pojos.TwitterStatus;
import fr.ippon.kafka.streams.utils.Audio;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static fr.ippon.kafka.streams.utils.Const.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

@Component
/**
 * Send a {@link SoundMessage.class} to SOUNDS_TOPIC for each category every WINDOWING_TIME.
 */
public class SoundsTopology implements CommandLineRunner {

    private KafkaStreams streams;

    @Override
    public void run(String... args) {

        // Create an instance of StreamsConfig from the Properties instance
        StreamsConfig config = new StreamsConfig(getProperties());
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        Random random = new Random();

        // Define custom serdes
        final Map<String, Object> serdeProps = new HashMap<>();
        final Serde<TwitterStatus> twitterStatusSerde = SerdeFactory.createSerde(TwitterStatus.class, serdeProps);
        final Serde<SoundPlayCount> soundPlayCountSerde = SerdeFactory.createSerde(SoundPlayCount.class, serdeProps);
        final Serde<TopSongs> topSongSerde = SerdeFactory.createSerde(TopSongs.class, serdeProps);
        final Serde<SoundMessage> soundMessageSerde = SerdeFactory.createSerde(SoundMessage.class, serdeProps);
        final Map<String, Integer> categories = Audio.retrieveAvailableCategories();
        final Serde<String> stringSerde = Serdes.String();
        final Serde<Windowed<String>> windowedSerde = new WindowedSerde<>(stringSerde);


        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                "http://localhost:8081"
        );


        final KStream<String, TwitterStatus> playEvent = streamsBuilder
                .stream(TWITTER_TOPIC, Consumed.with(stringSerde, twitterStatusSerde));

        KTable<Windowed<String>, Long> songPlayCount = playEvent
                // group by categories
                .groupBy((key, value) -> Audio.findCategory(value, categories), Serialized.with(stringSerde, twitterStatusSerde))
                .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOWING_TIME))) // Tumbling windowing
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(TWEET_PER_CATEGORY)
                        .withValueSerde(Serdes.Long()));


        KTable<Windowed<String>, TopSongs> songsKTable = songPlayCount
                .groupBy((windowedCategories, count) -> {
                            //we have to use a windowed key to run aggregation on timed windows
                            Windowed<String> windowedKey = new Windowed<>("TOPS", windowedCategories.window());
                            return KeyValue.pair(windowedKey, new SoundPlayCount(windowedCategories.key(), count));
                        },
                        Serialized.with(windowedSerde, soundPlayCountSerde))
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
        KTable<Windowed<String>, SoundMessage> topNSounds = songsKTable
                .mapValues(topSongs -> {
                    List<String> paths = StreamSupport
                            .stream(topSongs.spliterator(), false)
                            .limit(5)
                            .map(SoundPlayCount::getName)
                            .map(s -> String.format("%s/%s%d.ogg", s, s, random.nextInt(categories.get(s)) + 1))
                            .collect(toList());
                    return new SoundMessage(paths);
                });

        topNSounds
                .toStream()
                .map((windowedKey, message) -> KeyValue.pair("TOPS", message))
                .to(SOUNDS_TOPIC, Produced.with(stringSerde, soundMessageSerde));

        streams = new KafkaStreams(streamsBuilder.build(), config);

        // Clean local store between runs
        streams.cleanUp();
        streams.start();


    }

    public ReadOnlyKeyValueStore<Windowed<String>, TopSongs> getTopSongs() {
        return streams.store(TOP_SONG, QueryableStoreTypes.keyValueStore());
    }

    @PreDestroy
    public void destroy() {
        streams.close();
    }

    /**
     * Init stream properties.
     *
     * @return the created stream settings.
     */
    private static Properties getProperties() {


        Properties settings = new Properties();
        // Application ID, used for consumer groups
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "SoundsTopology");
        // Kafka bootstrap server (broker to talk to)
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // default serdes for serializing and deserializing key and value from and to streams
        settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        //ignore deserialization exception
        settings.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, SerdeException.class);

        // Enable exactly once
//        settings.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        // We can also set Consumer properties
        settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return settings;
    }
}
