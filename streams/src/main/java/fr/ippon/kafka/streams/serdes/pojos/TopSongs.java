package fr.ippon.kafka.streams.serdes.pojos;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.TreeSet;

public class TopSongs implements Iterable<SoundPlayCount> {

    private final Comparator<SoundPlayCount> comparator = (o1, o2) -> {
        int results = o2.getCount().compareTo(o1.getCount());
        if (results != 0) {
            return results;
        }
        return o1.getName().compareTo(o2.getName());
    };

    private final TreeSet<SoundPlayCount> songset = new TreeSet<>(comparator);

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
}
