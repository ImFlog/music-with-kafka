package fr.ippon.kafka.streams.processor;

import fr.ippon.kafka.streams.avro.SoundPlayCount;
import fr.ippon.kafka.streams.serdes.SerdeFactory;
import fr.ippon.kafka.streams.serdes.WindowedSerde;
import fr.ippon.kafka.streams.serdes.pojos.SoundMessage;
import fr.ippon.kafka.streams.serdes.TopSongSerde;
import fr.ippon.kafka.streams.serdes.pojos.TopSongs;
import fr.ippon.kafka.streams.serdes.pojos.TwitterStatus;
import fr.ippon.kafka.streams.utils.Audio;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.StreamSupport;

import static fr.ippon.kafka.streams.utils.Const.*;
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
        Map<String, Object> serdeProps = new HashMap<>();
        Serde<TwitterStatus> twitterStatusSerde = SerdeFactory.createSerde(TwitterStatus.class, serdeProps);
        Serde<SoundMessage> soundMessageSerde = SerdeFactory.createSerde(SoundMessage.class, serdeProps);
        Map<String, Integer> categories = Audio.retrieveAvailableCategories();
        TopSongSerde topSongSerde = new TopSongSerde();
        final Serde<Windowed<String>> windowedSerde = new WindowedSerde<>(Serdes.String());

        final Map<String, String> serdeConfig = Collections.singletonMap(
                AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                "http://localhost:8081"
        );

        final SpecificAvroSerde<SoundPlayCount> soundPlayCountSerde = new SpecificAvroSerde<>();
        soundPlayCountSerde.configure(serdeConfig, false);


        final KStream<String, TwitterStatus> playEvent = streamsBuilder
                .stream(TWITTER_TOPIC, Consumed.with(Serdes.String(), twitterStatusSerde));



        KTable<Windowed<String>, Long> songPlayCount = playEvent
                // group by categories
                .groupBy((key, value) -> Audio.findCategory(value, categories), Serialized.with(Serdes.String(), twitterStatusSerde))
                .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOWING_TIME))) // Tumbling windowing
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(TWEET_PER_CATEGORY)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()));


        KTable<Windowed<String>, TopSongs> songsKTable = songPlayCount
                .groupBy(
                        (windowedCategories, count) -> {
                            Windowed<String> windowed = new Windowed<>("TOPS", windowedCategories.window());
                            return KeyValue.pair(windowed, new SoundPlayCount(windowedCategories.key(), count));
                        },
                        Serialized.with(windowedSerde, soundPlayCountSerde)
                )
                .aggregate(
                        TopSongs::new,
                        //add sound to treeset order by vote
                        (key, value, aggregate) -> {
                            aggregate.add(value);
                            return aggregate;
                        },
                        (key, value, aggregate) -> {
                            aggregate.remove(value);
                            return aggregate;
                        },
                        Materialized.<Windowed<String>, TopSongs, KeyValueStore<Bytes, byte[]>>as(TOP_SONG)
                                .withKeySerde(windowedSerde)
                                .withValueSerde(topSongSerde)
                );


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
                .map((windowedKey, message) -> {
                    String key = windowedKey.key() + " @ " + windowedKey.window().start() + " -> " + windowedKey.window().end();
                    return KeyValue.pair(key, message);
                })
                .to(SOUNDS_TOPIC, Produced.with(Serdes.String(), soundMessageSerde));


        streams = new KafkaStreams(streamsBuilder.build(), config);
        // Clean local store between runs
        streams.cleanUp();
        streams.start();
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

        // Enable exactly once
//        settings.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        // We can also set Consumer properties
        settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return settings;
    }
}
