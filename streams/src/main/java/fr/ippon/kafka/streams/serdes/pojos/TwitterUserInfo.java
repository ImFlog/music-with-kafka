package fr.ippon.kafka.streams.serdes.pojos;

public class TwitterUserInfo {
    private String name;
    private String imgUri;
    private long tweetCount;

    public TwitterUserInfo(String name, String imgUri, long tweetCount) {
        this.name = name;
        this.imgUri = imgUri;
        this.tweetCount = tweetCount;
    }

    public TwitterUserInfo(String name, String imgUri) {
        this.name = name;
        this.imgUri = imgUri;
    }

    public TwitterUserInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    public long getTweetCount() {
        return tweetCount;
    }

    public void setTweetCount(long tweetCount) {
        this.tweetCount = tweetCount;
    }
}
