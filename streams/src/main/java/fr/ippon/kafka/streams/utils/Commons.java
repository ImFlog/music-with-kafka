package fr.ippon.kafka.streams.utils;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public final class Commons {

    public static <T, U> Stream<KeyValue<T, U>> iteratorToStream(KeyValueIterator<T, U> all) {
        return StreamSupport
                .stream(
                        Spliterators.spliteratorUnknownSize(all, Spliterator.ORDERED),
                        false
                );
    }

    public static String milliToDateString(long value) {
        return  LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneId.systemDefault()).toString();
    }

}
