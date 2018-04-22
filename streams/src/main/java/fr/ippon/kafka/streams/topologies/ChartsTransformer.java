package fr.ippon.kafka.streams.topologies;

import fr.ippon.kafka.streams.domains.chart.Chart;
import fr.ippon.kafka.streams.domains.chart.ChartMessage;
import fr.ippon.kafka.streams.utils.Commons;
import fr.ippon.kafka.streams.utils.Const;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;

import java.util.Comparator;

import static fr.ippon.kafka.streams.utils.Const.WINDOWING_TIME;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

public class ChartsTransformer implements Transformer<String, Long, KeyValue<String, ChartMessage>> {

    private static final Long DELTA = 200L;
    private static final Long INTERVAL_MIN = 5000L;
    private KeyValueStore<String, Long> kvStore;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        kvStore = (KeyValueStore<String, Long>) context.getStateStore(Const.CHART_PER_CATEGORY);
        Comparator<KeyValue<String, Long>> comparator = Comparator.comparingLong(kv -> kv.value);

        context.schedule(INTERVAL_MIN, PunctuationType.WALL_CLOCK_TIME, timestamp -> {
            ChartMessage charts = Commons
                    .iteratorToStream(kvStore.all())
                    .sorted(comparator.reversed())
                    .map(kv -> new Chart(kv.key, kv.value))
                    .collect(collectingAndThen(toList(), ChartMessage::new));
            if (charts.getCharts().isEmpty()) {
                System.out.println("Nothing to do, charts is empty");
            } else {
                context.forward("CHARTS", charts);
                context.commit();
            }
        });

        context.schedule(WINDOWING_TIME + DELTA, PunctuationType.WALL_CLOCK_TIME, timestamp -> {
            System.out.println("Clean state store");
            clean(kvStore);
        });
    }

    @Override
    public KeyValue<String, ChartMessage> transform(String key, Long value) {
        return null;
    }

    @Override
    public KeyValue<String, ChartMessage> punctuate(long timestamp) {
        return null;
    }


    @Override
    public void close() {

    }

    private void clean(KeyValueStore<String, Long> store) {
        Commons
                .iteratorToStream(store.all())
                .forEach(kv -> store.delete(kv.key));
    }

}
