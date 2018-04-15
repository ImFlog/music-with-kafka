package fr.ippon.kafka.streams.topologies;

import fr.ippon.kafka.streams.domains.Categories;
import fr.ippon.kafka.streams.domains.ChartMessage;
import fr.ippon.kafka.streams.domains.TwitterStatus;
import fr.ippon.kafka.streams.serdes.SerdeFactory;
import fr.ippon.kafka.streams.utils.Audio;
import fr.ippon.kafka.streams.utils.Commons;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
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
public class ChartsTopology implements CommandLineRunner {

    // Define custom serdes
    private final Map<String, Object> serdeProps = new HashMap<>();
    private final Serde<TwitterStatus> twitterStatusSerde = SerdeFactory.createSerde(TwitterStatus.class, serdeProps);
    private final Serde<ChartMessage> messageSerde = SerdeFactory.createSerde(ChartMessage.class, serdeProps);
    private final Serde<String> stringSerde = Serdes.String();
    private final Serde<Long> longSerde = Serdes.Long();
    private KafkaStreams stream;

    private static StreamsConfig kStreamConfig() {
        Properties settings = new Properties();
        // Application ID, used for consumer groups
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "ChartsTopology");
        // Kafka bootstrap server (broker to talk to)
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // default serdes for serializing and deserializing key and value from and to streams
        settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        settings.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());

        // We want the charts to be updated every 5 seconds
        settings.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1_000L);

        // Enable exactly once
//        settings.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        // We can also set Consumer properties
//        settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new StreamsConfig(settings);
    }

    @Override
    public void run(String... args) throws Exception {

        // Create an instance of StreamsConfig from the Properties instance
        StreamsConfig config = kStreamConfig();
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        Categories categories = Audio.retrieveAvailableCategories();

        final KStream<String, TwitterStatus> twitterStream = streamsBuilder
                .stream(TWITTER_TOPIC, Consumed.with(stringSerde, twitterStatusSerde));

        // 2. Group the stream by categories during the WINDOWING_TIME
        final KTable<String, Long> chartCount = twitterStream
                .filter((key, value) -> !Audio.UNKNOW.equalsIgnoreCase(Audio.findCategory(value, categories)))
                .groupBy((key, value) -> Audio.findCategory(value, categories),
                        Serialized.with(stringSerde, twitterStatusSerde))
                .count(Materialized.<String, Long, KeyValueStore<Bytes, byte[]>>as(CHART_PER_CATEGORY)
                        .withKeySerde(stringSerde)
                        .withValueSerde(longSerde));

        chartCount
                .toStream()
                .transform(ChartsTransformer::new, CHART_PER_CATEGORY)
                .to(CHARTS_TOPIC, Produced.with(stringSerde, messageSerde));

        stream = new KafkaStreams(streamsBuilder.build(), config);

        stream.cleanUp();
        stream.start();


    }

    public Stream<KeyValue<String, Long>> getChartsCountPerCategories() {
        KeyValueIterator<String, Long> it = chartsPerCategory().all();
        return Commons.iteratorToStream(it);
    }

    private ReadOnlyKeyValueStore<String, Long> chartsPerCategory() {
        return stream.store(CHART_PER_CATEGORY, QueryableStoreTypes.keyValueStore());
    }

    @PreDestroy
    public void destroy() {
        stream.close();
    }

}
