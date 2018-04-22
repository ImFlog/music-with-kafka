package fr.ippon.kafka.streams.domains.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@ToString
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitterStatus {
    private Long createdAt;

    private Long id;

    private String text;

    private String source;

    private boolean truncated;

    private boolean favorited;

    private boolean retweeted;

    private int favoriteCount;

    private TwitterUser user;

    private boolean retweet;

    private int retweetCount = 0;

    private List<HashtagEntity> hashtagEntities = new ArrayList<>();
}
