package fr.ippon.kafka.streams.topologies;

import fr.ippon.kafka.streams.domains.TwitterStatus;
import fr.ippon.kafka.streams.domains.TwitterUserInfo;
import fr.ippon.kafka.streams.domains.TwitterUserMessage;
import fr.ippon.kafka.streams.serdes.SerdeFactory;
import fr.ippon.kafka.streams.utils.Commons;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static fr.ippon.kafka.streams.utils.Const.*;

@Component

/**
 * Count the user who tweeted the most on #musicwithkafka.
 * Send the result to the topic USERS_TOPIC.
 */
public class UsersTopology implements CommandLineRunner {

    private static final String USER_FEED = "user-feed";
    private static final String USER_MESSAGE = "user-message";
    private static final String ALL_USERS = "all-users";
    private static final String APPLICATION_ID = "UsersTopology";
    private static final String BOOTSTRAP_SERVERS = "localhost:9092";
    // Define custom serdes
    private final Map<String, Object> serdeProps = new HashMap<>();
    private final Serde<TwitterStatus> twitterStatusSerde = SerdeFactory.createSerde(TwitterStatus.class, serdeProps);
    private final Serde<TwitterUserInfo> twitterUserInfoSerde = SerdeFactory.createSerde(TwitterUserInfo.class, serdeProps);
    private final Serde<TwitterUserMessage> twitterUserMessageSerde = SerdeFactory.createSerde(TwitterUserMessage.class, serdeProps);
    private final Serde<String> stringSerde = Serdes.String();
    private final Serde<Long> longSerde = Serdes.Long();
    private KafkaStreams streams;

    /**
     * Init stream properties.
     *
     * @return the created stream settings.
     */
    private static StreamsConfig kStreamConfig() {
        Properties settings = new Properties();
        // Application ID, used for consumer groups
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, APPLICATION_ID);
        // Kafka bootstrap server (broker to talk to)
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);

        // default serdes for serializing and deserializing key and value from and to streams
        settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        settings.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());

        // We want the users to be updated every 5 seconds
        settings.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 5_000L);

        // Enable exactly once
//        settings.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        // We can also set Consumer properties
        settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new StreamsConfig(settings);
    }

    public void run(String... args) {
        // Create an instance of StreamsConfig from the Properties instance
        final StreamsConfig config = kStreamConfig();
        final StreamsBuilder builder = new StreamsBuilder();

        // Simply read the stream
        final KStream<String, TwitterStatus> twitterStream = builder.stream(
                TWITTER_TOPIC,
                Consumed.with(stringSerde, twitterStatusSerde)
        );
//        twitterStream.print(Printed.toSysOut());

        // Feed the user-message topic
        twitterStream
                .map((key, twitterStatus) -> {
                    TwitterUserMessage userMessage = new TwitterUserMessage(
                            twitterStatus.getUser().getScreenName(),
                            twitterStatus.getText()
                    );
                    return KeyValue.pair(twitterStatus.getUser().getScreenName(), userMessage);
                })
                .to(USER_MESSAGE, Produced.with(stringSerde, twitterUserMessageSerde));

        //Feed the user store
        twitterStream
                .map((key, twitterStatus) -> {
                    TwitterUserInfo userInfo = new TwitterUserInfo(
                            twitterStatus.getUser().getScreenName(),
                            twitterStatus.getUser().getProfileImageURL()
                    );
                    return KeyValue.pair(twitterStatus.getUser().getScreenName(), userInfo);
                })
                .to(USER_FEED, Produced.with(stringSerde, twitterUserInfoSerde));

        //Construct a state store to hold all the users in the store
        final KTable<String, TwitterUserInfo> usersTable = builder
                .table(
                        USER_FEED,
                        Consumed.with(stringSerde, twitterUserInfoSerde),
                        Materialized.as(ALL_USERS)
                );

        //Join the tweet streams with our user state store to return a user with his tweets count
        final KStream<String, TwitterUserInfo> joinedStream = twitterStream
                .groupBy((key, twitterStatus) -> twitterStatus.getUser().getScreenName(), Serialized.with(stringSerde, twitterStatusSerde))
                .count(Materialized.as(TWEET_PER_USER))
                .toStream()
                .leftJoin(
                        usersTable,
                        (count, twitterUserInfo) -> {
                            twitterUserInfo.setTweetCount(count);
                            return twitterUserInfo;
                        },
                        Joined.with(stringSerde, longSerde, twitterUserInfoSerde)
                );

        joinedStream.to(USERS_TOPIC, Produced.with(stringSerde, twitterUserInfoSerde));

        streams = new KafkaStreams(builder.build(), config);
        // Clean local store between runs
        streams.cleanUp();
        streams.start();
    }

    public Stream<TwitterUserInfo> getTwitterUserInfoStream() {
        return Commons.iteratorToStream(getUserFeedStore().all()).map(kv -> kv.value);
    }

    public Stream<KeyValue<String, Long>> getTweetCountStream() {
        return Commons.iteratorToStream(getTweetCountPerUser().all());
    }

    @PreDestroy
    public void destroy() {
        streams.close();
    }

    private ReadOnlyKeyValueStore<String, Long> getTweetCountPerUser() {
        return streams.store(TWEET_PER_USER, QueryableStoreTypes.keyValueStore());
    }

    private ReadOnlyKeyValueStore<String, TwitterUserInfo> getUserFeedStore() {
        return streams.store(ALL_USERS, QueryableStoreTypes.keyValueStore());
    }

}
