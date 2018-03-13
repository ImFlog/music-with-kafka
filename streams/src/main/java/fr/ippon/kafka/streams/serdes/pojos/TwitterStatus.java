package fr.ippon.kafka.streams.serdes.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.ArrayList;
import java.util.List;

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

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(boolean truncated) {
        this.truncated = truncated;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public void setFavorited(boolean favorited) {
        this.favorited = favorited;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public void setRetweeted(boolean retweeted) {
        this.retweeted = retweeted;
    }

    public int getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(int favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public TwitterUser getUser() {
        return user;
    }

    public void setUser(TwitterUser user) {
        this.user = user;
    }

    public boolean isRetweet() {
        return retweet;
    }

    public void setRetweet(boolean retweet) {
        this.retweet = retweet;
    }

    public int getRetweetCount() {
        return retweetCount;
    }

    public void setRetweetCount(int retweetCount) {
        this.retweetCount = retweetCount;
    }

    public List<HashtagEntity> getHashtagEntities() {
        return hashtagEntities;
    }

    public void setHashtagEntities(ArrayList<HashtagEntity> hashtagEntities) {
        this.hashtagEntities = hashtagEntities;
    }

    @Override
    public String toString() {
        return "TwitterStatus{" +
                "createdAt=" + createdAt +
                ", id=" + id +
                ", text='" + text + '\'' +
                ", source='" + source + '\'' +
                ", truncated=" + truncated +
                ", favorited=" + favorited +
                ", retweeted=" + retweeted +
                ", favoriteCount=" + favoriteCount +
                ", user=" + user +
                ", retweet=" + retweet +
                ", retweetCount=" + retweetCount +
                ", hashtagEntities=" + hashtagEntities +
                '}';
    }
}
