package fr.ippon.kafka.streams.domains;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SoundPlayCount implements Comparable<SoundPlayCount> {

    private String name;

    private Long count;

    @Override
    public int compareTo(SoundPlayCount o) {
        int results = o.getCount().compareTo(getCount());
        return results != 0 ? results : getName().compareTo(o.getName());
    }
}
