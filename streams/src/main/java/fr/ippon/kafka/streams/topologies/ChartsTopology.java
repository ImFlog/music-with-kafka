package fr.ippon.kafka.streams.topologies;

import fr.ippon.kafka.streams.domains.*;
import fr.ippon.kafka.streams.serdes.SerdeFactory;
import fr.ippon.kafka.streams.serdes.WindowedSerde;
import fr.ippon.kafka.streams.utils.Audio;
import fr.ippon.kafka.streams.utils.Commons;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static fr.ippon.kafka.streams.utils.Const.*;

@Component
/**
 * Count tweets for a given category in a shorter window (WINDOWING_CHARTS_TIME).
 * Send them to the CHARTS_TOPIC
 */
public class ChartsTopology implements CommandLineRunner {

    private KafkaStreams streams;

    // Define custom serdes
    private final Map<String, Object> serdeProps = new HashMap<>();
    private final Serde<TwitterStatus> twitterStatusSerde = SerdeFactory.createSerde(TwitterStatus.class, serdeProps);
    private final Serde<Chart> chartSerde = SerdeFactory.createSerde(Chart.class, serdeProps);
    private final Serde<Charts> chartsSerde = SerdeFactory.createSerde(Charts.class, serdeProps);
    private final Serde<ChartMessage> messageSerde = SerdeFactory.createSerde(ChartMessage.class, serdeProps);
    private final Serde<String> stringSerde = Serdes.String();
    private final Serde<Windowed<String>> windowedSerde = new WindowedSerde<>(stringSerde);
    private final Serde<Long> longSerde = Serdes.Long();


    @Override
    public void run(String... args) {
        // Create an instance of StreamsConfig from the Properties instance
        StreamsConfig config = kStreamConfig();
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        Categories categories = Audio.retrieveAvailableCategories();


        final KStream<String, TwitterStatus> twitterStream = streamsBuilder
                .stream(TWITTER_TOPIC, Consumed.with(stringSerde, twitterStatusSerde));


        // 2. Group the stream by categories during the WINDOWING_TIME
        final KTable<Windowed<String>, Long> chartCount = twitterStream
                .groupBy((key, value) -> Audio.findCategory(value, categories),
                        Serialized.with(Serdes.String(), twitterStatusSerde))
                .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOWING_TIME)))// Tumbling windowing
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(CHART_PER_CATEGORY)
                        .withKeySerde(stringSerde)
                        .withValueSerde(longSerde));


        chartCount
                .groupBy((windowedKey, count) -> {
                    Windowed<String> windowed = new Windowed<>("CHARTS", windowedKey.window());
                    return KeyValue.pair(windowed, new Chart(windowedKey.key(), count));
                }, Serialized.with(windowedSerde, chartSerde))
                .aggregate(
                        Charts::new,
                        (key, value, aggregate) -> {
                            aggregate.add(value);
                            return aggregate;
                        },
                        (key, value, aggregate) -> {
                            aggregate.remove(value);
                            return aggregate;
                        },
                        Materialized.<Windowed<String>, Charts, KeyValueStore<Bytes, byte[]>>as("chartsSong")
                                .withValueSerde(chartsSerde)
                )
                .mapValues(charts -> {
                    List<Chart> list = charts.toStream()
                            .collect(Collectors.toList());
                    return new ChartMessage(list);
                })
                .toStream()
                .map((windowedKey, value) -> KeyValue.pair(windowedKey.key(), value))
                .to(CHARTS_TOPIC, Produced.with(stringSerde, messageSerde));

        streams = new KafkaStreams(streamsBuilder.build(), config);
        // Clean local store between runs
        streams.cleanUp();
        streams.start();
    }

    @PreDestroy
    public void destroy() {
        streams.close();
    }

    public Stream<Chart> getLastChartSong() {
        return Commons.iteratorToStream(chartsSong().all())
                .max(Comparator.comparingLong(kv -> kv.key.window().start()))
                .map(kv -> kv.value)
                .orElseGet(Charts::new)
                .toStream();
    }

    public Stream<KeyValue<Long, Long>> getChartsCountPerCategories(String category) {
        WindowStoreIterator<Long> it = chartsPerCategory().fetch(category, 0, System.currentTimeMillis());
        return Commons.iteratorToStream(it);
    }

    private ReadOnlyWindowStore<String, Long> chartsPerCategory() {
        return streams.store(CHART_PER_CATEGORY, QueryableStoreTypes.windowStore());
    }

    private ReadOnlyKeyValueStore<Windowed<String>, Charts> chartsSong() {
        return streams.store("chartsSong", QueryableStoreTypes.keyValueStore());
    }

    /**
     * Init stream properties.
     *
     * @return the created stream settings.
     */
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
        settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new StreamsConfig(settings);
    }

}
