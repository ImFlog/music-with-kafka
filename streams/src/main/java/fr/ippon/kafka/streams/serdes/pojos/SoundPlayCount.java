package fr.ippon.kafka.streams.serdes.pojos;

public class SoundPlayCount implements Comparable<SoundPlayCount> {

    private String name;
    private Long count;

    public SoundPlayCount() {
    }

    public SoundPlayCount(String name, Long count) {
        this.name = name;
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    @Override
    public int compareTo(SoundPlayCount o) {
        int results = o.getCount().compareTo(getCount());
        if (results != 0) {
            return results;
        }
        return getName().compareTo(o.getName());
    }
}
