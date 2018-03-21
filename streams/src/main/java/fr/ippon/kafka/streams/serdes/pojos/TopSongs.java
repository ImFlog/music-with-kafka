package fr.ippon.kafka.streams.serdes.pojos;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.TreeSet;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public class TopSongs implements Iterable<SoundPlayCount> {

    private final TreeSet<SoundPlayCount> songset = new TreeSet<>();

    public void add(final SoundPlayCount soundPlayCount) {
        songset.add(soundPlayCount);
    }

    public void remove(final SoundPlayCount song) {
        songset.remove(song);
    }

    @Override
    public Spliterator<SoundPlayCount> spliterator() {
        return songset.spliterator();
    }

    @Override
    public Iterator<SoundPlayCount> iterator() {
        return songset.iterator();
    }

    public Iterator<SoundPlayCount> getSongset() {
        return songset.iterator();
    }

}
