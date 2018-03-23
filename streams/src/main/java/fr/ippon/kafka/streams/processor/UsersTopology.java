package fr.ippon.kafka.streams.processor;

import fr.ippon.kafka.streams.serdes.SerdeFactory;
import fr.ippon.kafka.streams.serdes.pojos.TwitterStatus;
import fr.ippon.kafka.streams.serdes.pojos.TwitterUserInfo;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static fr.ippon.kafka.streams.utils.Const.*;

@Component

/**
 * Count the user who tweeted the most on #musicwithkafka.
 * Send the result to the topic USERS_TOPIC.
 */
public class UsersTopology implements CommandLineRunner {

    private static final String USER_FEED = "user-feed";
    private static final String ALL_USERS = "all-users";
    private KafkaStreams streams;

    public void run(String... args) {
        // Create an instance of StreamsConfig from the Properties instance
        StreamsConfig config = new StreamsConfig(getProperties());
        StreamsBuilder builder = new StreamsBuilder();

        // Define custom serdes
        final Map<String, Object> serdeProps = new HashMap<>();
        final Serde<TwitterStatus> twitterStatusSerde = SerdeFactory.createSerde(TwitterStatus.class, serdeProps);
        final Serde<TwitterUserInfo> twitterUserInfoSerde = SerdeFactory.createSerde(TwitterUserInfo.class, serdeProps);
        final Serde<String> stringSerde = Serdes.String();
        final Serde<Long> longSerde = Serdes.Long();

        // Simply read the stream
        KStream<String, TwitterStatus> twitterStream = builder.stream(
                TWITTER_TOPIC,
                Consumed.with(stringSerde, twitterStatusSerde)
        );
        twitterStream.print(Printed.toSysOut());

        //Construct a state store to hold all the users in the store
        KTable<String, TwitterUserInfo> usersTable = builder
                .table(
                        USER_FEED,
                        Consumed.with(stringSerde, twitterUserInfoSerde),
                        Materialized.as(ALL_USERS)
                );

        //Feed the user store
        twitterStream
                .map((k, v) -> {
                    TwitterUserInfo userInfo = new TwitterUserInfo(
                            v.getUser().getScreenName(),
                            v.getUser().getProfileImageURL()
                    );
                    return KeyValue.pair(v.getUser().getScreenName(), userInfo);
                }).to(USER_FEED, Produced.with(stringSerde, twitterUserInfoSerde));


        //Join the tweet streams with our user state store to return a user with his tweets count
        KStream<String, TwitterUserInfo> joinedStream = twitterStream
                .groupBy((key, value) -> value.getUser().getScreenName(), Serialized.with(stringSerde, twitterStatusSerde))
                .count(Materialized.as(TWEET_PER_USER))
                .toStream()
                .leftJoin(
                        usersTable,
                        (v, info) -> {
                            info.setTweetCount(v);
                            return info;
                        },
                        Joined.with(stringSerde, longSerde, twitterUserInfoSerde)
                );

        //TODO: the send to USER_FEED update the user state store. Will we keep this ?
        joinedStream.to(USER_FEED, Produced.with(stringSerde, twitterUserInfoSerde));

        joinedStream.to(USERS_TOPIC, Produced.with(stringSerde, twitterUserInfoSerde));

        streams = new KafkaStreams(builder.build(), config);
        // Clean local store between runs
        streams.cleanUp();
        streams.start();
    }

    public ReadOnlyKeyValueStore<String, Long> getTweetCountPerUser() {
        return streams.store(TWEET_PER_USER, QueryableStoreTypes.keyValueStore());
    }

    public ReadOnlyKeyValueStore<String, TwitterUserInfo> getUserFeedStore() {
        return streams.store(ALL_USERS, QueryableStoreTypes.keyValueStore());
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
