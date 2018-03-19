package fr.ippon.kafka.streams.serdes;

import fr.ippon.kafka.streams.serdes.pojos.SoundPlayCount;
import fr.ippon.kafka.streams.serdes.pojos.TopSongs;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;

public class TopSongSerde implements Serde<TopSongs> {
    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

    }

    @Override
    public void close() {

    }

    @Override
    public Serializer<TopSongs> serializer() {
        return new Serializer<TopSongs>() {
            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {

            }

            @Override
            public byte[] serialize(String topic, TopSongs data) {
                final ByteArrayOutputStream out = new ByteArrayOutputStream();
                final DataOutputStream dataOutputStream = new DataOutputStream(out);
                try {
                    for (SoundPlayCount songPlayCount : data) {
                        byte[] bytes = songPlayCount.getName().getBytes("UTF-8");
                        dataOutputStream.writeInt(bytes.length);
                        dataOutputStream.write(bytes);
                        dataOutputStream.writeLong(songPlayCount.getCount());
                    }
                    dataOutputStream.flush();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return out.toByteArray();
            }

            @Override
            public void close() {

            }
        };
    }

    @Override
    public Deserializer<TopSongs> deserializer() {
        return new Deserializer<TopSongs>() {
            @Override
            public void configure(Map<String, ?> configs, boolean isKey) {

            }

            @Override
            public TopSongs deserialize(String topic, byte[] bytes) {
                if (bytes == null || bytes.length == 0) {
                    return null;
                }
                final DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(bytes));
                TopSongs result = new TopSongs();
                try {
                    while (inputStream.available() > 0) {
                        int nameLength = inputStream.readInt();
                        byte[] data = new byte[nameLength];
                        inputStream.readFully(data);
                        long count = inputStream.readLong();
                        result.add(new SoundPlayCount(new String(data, "UTF-8"), count));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return result;
            }

            @Override
            public void close() {

            }
        };
    }
}
