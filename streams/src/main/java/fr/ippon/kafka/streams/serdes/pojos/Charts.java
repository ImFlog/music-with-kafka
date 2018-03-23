package fr.ippon.kafka.streams.serdes.pojos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.*;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class Charts implements Iterable<Chart> {

    private final TreeSet<Chart> chartset = new TreeSet<>();

    public void add(Chart chart) {
        chartset.add(chart);
    }

    public void remove(Chart chart) {
        chartset.remove(chart);
    }

    @Override
    public Spliterator<Chart> spliterator() {
        return chartset.spliterator();
    }

    @Override
    public Iterator<Chart> iterator() {
        return chartset.iterator();
    }

    public Iterator<Chart> getChartset() {
        return chartset.iterator();
    }

    public Stream<Chart> toStream() {
        return StreamSupport.stream(chartset.spliterator(), false);
    }

}
