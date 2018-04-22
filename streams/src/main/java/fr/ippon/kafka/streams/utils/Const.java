package fr.ippon.kafka.streams.utils;

public final class Const {

    public static final Long WINDOWING_TIME = 30000L;

    // Input topics
    public static final String TWITTER_TOPIC = "twitter_json";
    // Output topics
    public static final String SOUNDS_TOPIC = "sounds";
    public static final String USERS_TOPIC = "users";
    public static final String CHARTS_TOPIC = "charts";

    // Store names
    public static final String TWEET_PER_USER = "tweetPerUser";
    public static final String TWEET_PER_CATEGORY = "tweetPerCategory";
    public static final String CHART_PER_CATEGORY = "chartPerCategory";
    public static final String TOP_SONG = "topSong";

    private Const() {
    }
}
