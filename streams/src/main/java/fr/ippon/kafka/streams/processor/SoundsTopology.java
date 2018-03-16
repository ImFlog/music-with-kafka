package fr.ippon.kafka.streams.processor;

import fr.ippon.kafka.streams.serdes.SerdeFactory;
import fr.ippon.kafka.streams.serdes.pojos.SoundMessage;
import fr.ippon.kafka.streams.serdes.pojos.TwitterStatus;
import fr.ippon.kafka.streams.utils.Audio;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.apache.kafka.streams.state.WindowStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static fr.ippon.kafka.streams.utils.Const.SOUNDS_TOPIC;
import static fr.ippon.kafka.streams.utils.Const.TWEET_PER_CATEGORY;
import static fr.ippon.kafka.streams.utils.Const.TWITTER_TOPIC;
import static fr.ippon.kafka.streams.utils.Const.WINDOWING_TIME;

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

        // Define custom serdes
        Map<String, Object> serdeProps = new HashMap<>();
        Serde<TwitterStatus> twitterStatusSerde = SerdeFactory.createSerde(TwitterStatus.class, serdeProps);
        Serde<SoundMessage> soundMessageSerde = SerdeFactory.createSerde(SoundMessage.class, serdeProps);

        Map<String, Integer> categories = Audio.retrieveAvailableCategories();
        streamsBuilder
                .stream(TWITTER_TOPIC, Consumed.with(Serdes.String(), twitterStatusSerde))
                // group by categories
                .groupBy((key, value) -> Audio.findCategory(value, categories),
                        Serialized.with(Serdes.String(), twitterStatusSerde))
                .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOWING_TIME))) // Tumbling windowing
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(TWEET_PER_CATEGORY)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()))
                // TODO : add aggregation
                .toStream()
                .map((windowedKey, value) ->
                        new KeyValue<>(windowedKey.key() + "@" + windowedKey.window().start() + "->" + windowedKey.window().end(),
                                new SoundMessage(String.format("%s/%s%d.ogg",
                                        windowedKey.key(),
                                        windowedKey.key(),
                                        value % categories.get(windowedKey.key())))))
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
