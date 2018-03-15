package fr.ippon.kafka.streams.processor;

import static fr.ippon.kafka.streams.serdes.SerdeFactory.createWindowedStringSerde;

import fr.ippon.kafka.streams.serdes.SerdeFactory;
import fr.ippon.kafka.streams.serdes.pojos.SoundMessage;
import fr.ippon.kafka.streams.serdes.pojos.TwitterStatus;
import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KGroupedStream;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StreamProcessor implements CommandLineRunner {

    private static final Long WINDOWING_SOUNDS_TIME = 30L;
    private static final Long WINDOWING_CHARTS_TIME = 10L;

    // Input topics
    private static final String TWITTER_TOPIC = "twitter_json";
    // Output topics
    private static final String SOUNDS_TOPIC = "sounds";
    private static final String USERS_TOPIC = "users";
    private static final String CHARTS_TOPIC = "charts";

    // Store names
    private static final String TWEET_PER_USER = "tweetPerUser";
    private static final String TWEET_PER_CATEGORY = "tweetPerCategory";

    private KafkaStreams streams;

    public void run(String... args) {
        Map<String, Integer> categories = retrieveAvailableCategories();

        // Define custom serdes
        Map<String, Object> serdeProps = new HashMap<>();
        Serde<TwitterStatus> twitterStatusSerde = SerdeFactory.createSerde(TwitterStatus.class, serdeProps);
        Serde<SoundMessage> soundMessageSerde = SerdeFactory.createSerde(SoundMessage.class, serdeProps);
        Serde<Windowed<String>> windowedStringSerde = createWindowedStringSerde(WINDOWING_SOUNDS_TIME);

        // Create an instance of StreamsConfig from the Properties instance
        StreamsConfig config = new StreamsConfig(getProperties());
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // 1. Simply read the stream
        KStream<String, TwitterStatus> twitterStream = streamsBuilder.stream(
            TWITTER_TOPIC,
            Consumed.with(Serdes.String(), twitterStatusSerde));
        twitterStream.print(Printed.toSysOut());

        // 2. Divide the stream per category.
        KGroupedStream<String, TwitterStatus> tweetsGroupByCategories =
            twitterStream.groupBy((key, value) -> findCategory(value, categories),
                Serialized.with(Serdes.String(), twitterStatusSerde));

        // 3. Send a message to SOUNDS_TOPIC for each category every WINDOWING_SOUNDS_TIME.
        tweetsGroupByCategories
            .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOWING_SOUNDS_TIME))) // Tumbling windowing
            .count(Materialized.with(Serdes.String(), Serdes.Long()))
            .toStream()
            .map((windowedKey, value) -> new KeyValue<>(windowedKey, new SoundMessage(String.format("%s/%s%d.ogg",
                windowedKey.key(),
                windowedKey.key(),
                value % categories.get(windowedKey.key())))))
            .to(SOUNDS_TOPIC, Produced.with(windowedStringSerde, soundMessageSerde));

        // 4. Count tweets for a given category in a shorter window (WINDOWING_CHARTS_TIME). Send them to the CHARTS_TOPIC
        tweetsGroupByCategories
            .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOWING_CHARTS_TIME)))// Tumbling windowing
            .count()
            .toStream()
            .groupBy((key, value) -> key.key(), Serialized.with(Serdes.String(), Serdes.Long()))
            .count(Materialized.as(TWEET_PER_CATEGORY))
            .toStream()
            .to(CHARTS_TOPIC, Produced.with(Serdes.String(), Serdes.Long()));

        // 5. Count the user who tweeted the most on #musicwithkafka
        twitterStream
            .groupBy((key, value) -> value.getUser().getName(), Serialized.with(Serdes.String(), twitterStatusSerde))
            .count(Materialized.as(TWEET_PER_USER))
            .toStream()
            .to(USERS_TOPIC, Produced.valueSerde(Serdes.Long()));

        streams = new KafkaStreams(streamsBuilder.build(), config);
        // Clean local store between runs
        streams.cleanUp();
        streams.start();
    }

    /**
     * Finds the category associated to a tweet
     *
     * @param value Tweet
     * @param categories Map of available categories
     * @return The category key if found. Null if not
     */
    private String findCategory(TwitterStatus value, Map<String, Integer> categories) {
        for (Map.Entry<String, Integer> category : categories.entrySet()) {
            if (matchCategory(category, value)) {
                return category.getKey();
            }
        }
        return null;
    }

    /**
     * Check if it matches a category.
     * "_" and "-" are matched as space too.
     */
    private boolean matchCategory(Map.Entry<String, Integer> category, TwitterStatus value) {
        String tweetText = value.getText().toLowerCase();
        return tweetText.contains(category.getKey()) ||
            tweetText.contains(category.getKey()
                .replace("-", " ")
                .replace("_", " "));
    }

    /**
     * Read audio directory and count the number of .ogg files in each directory.
     * The string will be used to split tweets.
     */
    private Map<String, Integer> retrieveAvailableCategories() {
        Map<String, Integer> categoriesAndCount = new HashMap<>();
        File[] filesList = new File("../audio/").listFiles();
        if (filesList != null) {
            Arrays.stream(filesList).forEach(f -> {
                if (f.isDirectory()) {
                    categoriesAndCount.put(f.getName(), countSubAudioFiles(f));
                }
            });
        }
        return categoriesAndCount;
    }

    private Integer countSubAudioFiles(File file) {
        File[] subFiles = file.listFiles();
        if (subFiles == null) {
            return 0;
        }
        return subFiles.length - 1;
    }

    public ReadOnlyKeyValueStore<String, Long> getTweetCountPerUser() {
        return streams.store(TWEET_PER_USER, QueryableStoreTypes.keyValueStore());
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
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "TwitterStreamingTest");
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
