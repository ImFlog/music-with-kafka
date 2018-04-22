package fr.ippon.kafka.streams.domains.chart;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Chart implements Comparable<Chart> {

    private String sound;
    private Long count = 0L;

    @Override
    public int compareTo(Chart o) {
        int results = o.getCount().compareTo(getCount());
        return results != 0
                ? results
                : getSound().compareTo(o.getSound());
    }
}
