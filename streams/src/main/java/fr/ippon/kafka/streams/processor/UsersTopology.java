package fr.ippon.kafka.streams.processor;

import fr.ippon.kafka.streams.serdes.SerdeFactory;
import fr.ippon.kafka.streams.serdes.pojos.TwitterStatus;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Printed;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static fr.ippon.kafka.streams.utils.Const.TWEET_PER_USER;
import static fr.ippon.kafka.streams.utils.Const.TWITTER_TOPIC;
import static fr.ippon.kafka.streams.utils.Const.USERS_TOPIC;

@Component

/**
 * Count the user who tweeted the most on #musicwithkafka.
 * Send the result to the topic USERS_TOPIC.
 */
public class UsersTopology implements CommandLineRunner {


    private KafkaStreams streams;

    public void run(String... args) {
        // Create an instance of StreamsConfig from the Properties instance
        StreamsConfig config = new StreamsConfig(getProperties());
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // Define custom serdes
        Map<String, Object> serdeProps = new HashMap<>();
        Serde<TwitterStatus> twitterStatusSerde = SerdeFactory.createSerde(TwitterStatus.class, serdeProps);

        // Simply read the stream
        KStream<String, TwitterStatus> twitterStream = streamsBuilder.stream(
                TWITTER_TOPIC,
                Consumed.with(Serdes.String(), twitterStatusSerde));
        twitterStream.print(Printed.toSysOut());

        twitterStream
                .groupBy((key, value) -> value.getUser().getScreenName(), Serialized.with(Serdes.String(), twitterStatusSerde))
                .count(Materialized.as(TWEET_PER_USER))
                .toStream()
                .to(USERS_TOPIC, Produced.valueSerde(Serdes.Long()));

        streams = new KafkaStreams(streamsBuilder.build(), config);
        // Clean local store between runs
        streams.cleanUp();
        streams.start();
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
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "UsersTopology");
        // Kafka bootstrap server (broker to talk to)
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // default serdes for serializing and deserializing key and value from and to streams
        settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // We want the users to be updated every 5 seconds
        settings.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 5_000L);

        // Enable exactly once
//        settings.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        // We can also set Consumer properties
        settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return settings;
    }
}
