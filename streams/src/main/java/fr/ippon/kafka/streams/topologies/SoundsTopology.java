package fr.ippon.kafka.streams.topologies;

import fr.ippon.kafka.streams.domains.SoundMessage;
import fr.ippon.kafka.streams.domains.SoundPlayCount;
import fr.ippon.kafka.streams.domains.TwitterStatus;
import fr.ippon.kafka.streams.serdes.SerdeException;
import fr.ippon.kafka.streams.serdes.SerdeFactory;
import fr.ippon.kafka.streams.utils.Commons;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import static fr.ippon.kafka.streams.utils.Const.*;

@Component
public class SoundsTopology implements CommandLineRunner {

    private static final String APPLICATION_ID_VALUE = "SoundsTopology";
    private static final String BOOTSTRAP_SERVERS_VALUE = "localhost:9092";
    private static final String AUTO_OFFSET_VALUE = "earliest";
    private static final String SOURCE_NODE = "Source";
    private static final String PROCESS_NODE = "Process";
    private static final String SINK_NODE = "Sink";


    private KafkaStreams streams;

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


        // Enable exactly once
//        settings.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        // We can also set Consumer properties
//        settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, AUTO_OFFSET_VALUE);
        return new StreamsConfig(settings);
    }

    @Override
    public void run(String... args) {

        // Define custom serdes
        final Map<String, Object> serdeProps = new HashMap<>();
        final Serde<TwitterStatus> twitterStatusSerde = SerdeFactory.createSerde(TwitterStatus.class, serdeProps);
        final Serde<SoundMessage> soundMessageSerde = SerdeFactory.createSerde(SoundMessage.class, serdeProps);
        final Serde<String> stringSerde = Serdes.String();
        final Serde<Long> longSerde = Serdes.Long();

        // Create an instance of StreamsConfig from the Properties instance
        StreamsConfig config = kStreamConfig();

        Topology topology = new Topology();

        KeyValueBytesStoreSupplier persistentKeyValueStore = Stores.persistentKeyValueStore(TOP_SONG);

        StoreBuilder<KeyValueStore<String, Long>> keyValueStoreStoreBuilder = Stores.keyValueStoreBuilder(
                persistentKeyValueStore,
                stringSerde,
                longSerde
        );

        topology
                .addSource(SOURCE_NODE, stringSerde.deserializer(), twitterStatusSerde.deserializer(), TWITTER_TOPIC)
                .addProcessor(PROCESS_NODE, SoundsProcessor::new, SOURCE_NODE)
                .addStateStore(keyValueStoreStoreBuilder, PROCESS_NODE)
                .addSink(SINK_NODE, SOUNDS_TOPIC, stringSerde.serializer(), soundMessageSerde.serializer(), PROCESS_NODE);


        streams = new KafkaStreams(topology, config);


        // Clean local store between runs
        streams.cleanUp();
        streams.start();


    }

    @PreDestroy
    public void destroy() {
        streams.close();
    }

    public Stream<SoundPlayCount> getLastTopSongs() {
        Comparator<KeyValue<String, Long>> comparator = Comparator.comparingLong(kv -> kv.value);
        return Commons.iteratorToStream(getTopSongsStore().all())
                .sorted(comparator.reversed())
                .limit(5)
                .map(kv -> new SoundPlayCount(kv.key, kv.value));
    }

    public Stream<String> getTweetsPerCategories(String key) {
        return Commons.iteratorToStream(getTweetsStore().fetch(key, 0, System.currentTimeMillis()))
                .map(k -> "times = " + k.key + " - count = " + k.value);
    }

    private ReadOnlyKeyValueStore<String, Long> getTopSongsStore() {
        return streams.store(TOP_SONG, QueryableStoreTypes.keyValueStore());
    }

    private ReadOnlyWindowStore<String, Long> getTweetsStore() {
        return streams.store(TWEET_PER_CATEGORY, QueryableStoreTypes.windowStore());
    }

}
