package fr.ippon.kafka.streams.domains.twitter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TwitterUserInfo {
    private String name;

    private String imgUri;

    private long tweetCount;

    public TwitterUserInfo(String name, String imgUri) {
        this.name = name;
        this.imgUri = imgUri;
    }

}
