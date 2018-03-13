package fr.ippon.kafka.streams.serdes.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class HashtagEntity {
    private String text;
    private int start = 0;
    private int end = 0;

    public HashtagEntity() {
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "HashtagEntity{" +
                "text='" + text + '\'' +
                ", start=" + start +
                ", end=" + end +
                '}';
    }
}
