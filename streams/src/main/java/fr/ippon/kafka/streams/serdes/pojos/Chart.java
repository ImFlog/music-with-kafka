package fr.ippon.kafka.streams.serdes.pojos;

public class Chart implements Comparable<Chart> {
    private String sound;
    private Long count = 0L;

    public Chart() {
    }

    public Chart(String sound, Long count) {
        this.sound = sound;
        this.count = count;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public int compareTo(Chart o) {
        int results = o.getCount().compareTo(getCount());
        if (results != 0) {
            return results;
        }
        return getSound().compareTo(o.getSound());
    }
}
