package fr.ippon.kafka.streams.topologies;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import fr.ippon.kafka.streams.domains.Chart;
import fr.ippon.kafka.streams.domains.ChartMessage;
import fr.ippon.kafka.streams.utils.Commons;
import fr.ippon.kafka.streams.utils.Const;
import java.util.Comparator;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueStore;

public class ChartsTransformer implements Transformer<String, Long, KeyValue<String, ChartMessage>> {

    private static Long INTERVAL = 30300L;
    private static Long INTERVAL_MIN = 5000L;
    private ProcessorContext ctx;
    private KeyValueStore<String, Long> kvStore;

    @Override
    public void init(ProcessorContext context) {
        ctx = context;
        kvStore = (KeyValueStore<String, Long>) ctx.getStateStore(Const.CHART_PER_CATEGORY);
        Comparator<KeyValue<String, Long>> comparator = Comparator.comparingLong(kv -> kv.value);

        ctx.schedule(INTERVAL_MIN, PunctuationType.WALL_CLOCK_TIME, timestamp -> {
            System.out.println("TRANSFORMER PUNCTUATE");
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

        ctx.schedule(INTERVAL, PunctuationType.WALL_CLOCK_TIME, timestamp -> {
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
