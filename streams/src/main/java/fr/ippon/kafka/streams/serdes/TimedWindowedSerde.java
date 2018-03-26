package fr.ippon.kafka.streams.serdes;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.streams.kstream.Windowed;

import java.util.Map;

public class TimedWindowedSerde<T> implements Serde<Windowed<T>> {

    private final Serde<Windowed<T>> inner;

    public TimedWindowedSerde(Serde<T> serde) {
        inner = Serdes.serdeFrom(
                new TimeWindowedSerializer<>(serde.serializer()),
                new TimeWindowedDeserializer<>(serde.deserializer()));
    }

    @Override
    public Serializer<Windowed<T>> serializer() {
        return inner.serializer();
    }

    @Override
    public Deserializer<Windowed<T>> deserializer() {
        return inner.deserializer();
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        inner.serializer().configure(configs, isKey);
        inner.deserializer().configure(configs, isKey);
    }

    @Override
    public void close() {
        inner.serializer().close();
        inner.deserializer().close();
    }

}