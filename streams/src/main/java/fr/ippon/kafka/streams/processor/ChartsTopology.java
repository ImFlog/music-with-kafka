package fr.ippon.kafka.streams.processor;

import fr.ippon.kafka.streams.serdes.SerdeFactory;
import fr.ippon.kafka.streams.serdes.WindowedSerde;
import fr.ippon.kafka.streams.serdes.pojos.Chart;
import fr.ippon.kafka.streams.serdes.pojos.ChartMessage;
import fr.ippon.kafka.streams.serdes.pojos.Charts;
import fr.ippon.kafka.streams.serdes.pojos.TwitterStatus;
import fr.ippon.kafka.streams.utils.Audio;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static fr.ippon.kafka.streams.utils.Const.*;

@Component
/**
 * Count tweets for a given category in a shorter window (WINDOWING_CHARTS_TIME).
 * Send them to the CHARTS_TOPIC
 */
public class ChartsTopology implements CommandLineRunner {

    private KafkaStreams streams;

    @Override
    public void run(String... args) {
        // Create an instance of StreamsConfig from the Properties instance
        StreamsConfig config = new StreamsConfig(getProperties());
        StreamsBuilder streamsBuilder = new StreamsBuilder();

        // Define custom serdes
        Map<String, Object> serdeProps = new HashMap<>();
        Serde<TwitterStatus> twitterStatusSerde = SerdeFactory.createSerde(TwitterStatus.class, serdeProps);
        Serde<Chart> chartSerde = SerdeFactory.createSerde(Chart.class, serdeProps);
        Serde<Charts> chartsSerde = SerdeFactory.createSerde(Charts.class, serdeProps);
        Serde<ChartMessage> messageSerde = SerdeFactory.createSerde(ChartMessage.class, serdeProps);
        final Serde<Windowed<String>> windowedSerde = new WindowedSerde<>(Serdes.String());


        Map<String, Integer> categories = Audio.retrieveAvailableCategories();
        streamsBuilder
                .stream(TWITTER_TOPIC, Consumed.with(Serdes.String(), twitterStatusSerde))
                // 2. Divide the stream per category.
                .groupBy((key, value) -> Audio.findCategory(value, categories),
                        Serialized.with(Serdes.String(), twitterStatusSerde))
                .windowedBy(TimeWindows.of(TimeUnit.SECONDS.toMillis(WINDOWING_TIME)))// Tumbling windowing
                .count(Materialized.<String, Long, WindowStore<Bytes, byte[]>>as(CHART_PER_CATEGORY)
                        .withKeySerde(Serdes.String())
                        .withValueSerde(Serdes.Long()))

                .groupBy((k, v) -> {
                    Windowed<String> windowed = new Windowed<>("CHARTS", k.window());
                    return KeyValue.pair(windowed, new Chart(k.key(), v));
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
                            .map(c -> new Chart(c.getSound(), c.getCount()))
                            .collect(Collectors.toList());
                    return new ChartMessage(list);
                })

                .toStream()
                .map((windowedKey, value) -> KeyValue.pair(windowedKey.key(), value))
                .to(CHARTS_TOPIC, Produced.with(Serdes.String(), messageSerde));

        streams = new KafkaStreams(streamsBuilder.build(), config);
        // Clean local store between runs
        streams.cleanUp();
        streams.start();
    }

    public ReadOnlyWindowStore<String, Long> chartsStore() {
        return streams.store(CHART_PER_CATEGORY, QueryableStoreTypes.windowStore());
    }

    public ReadOnlyKeyValueStore<Windowed<String>, Charts> chartsStoreWindowed() {
        return streams.store("chartsSong", QueryableStoreTypes.keyValueStore());
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
        settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "ChartsTopology");
        // Kafka bootstrap server (broker to talk to)
        settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        // default serdes for serializing and deserializing key and value from and to streams
        settings.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        settings.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());

        // We want the charts to be updated every 5 seconds
        settings.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 5_000L);

        // Enable exactly once
//        settings.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        // We can also set Consumer properties
        settings.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return settings;
    }
}
