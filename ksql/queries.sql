-- Set properties
SET 'auto.offset.reset' = 'earliest';

-- Read twitter stream
CREATE STREAM twitter_raw (CreatedAt BIGINT, Id BIGINT, Text VARCHAR, User VARCHAR) \
WITH (KAFKA_TOPIC='twitter_json', VALUE_FORMAT='JSON');

-- Create a derived stream with user values & text only
CREATE STREAM twitter_clean AS \
SELECT TIMESTAMPTOSTRING(CreatedAt, 'yyyy-MM-dd HH:mm:ss.SSS') AS CreatedAt,\
    EXTRACTJSONFIELD(User,'$.Name') AS user_name,\
    EXTRACTJSONFIELD(User,'$.ScreenName') AS user_screenName,\
    EXTRACTJSONFIELD(User,'$.ProfileImageURL') AS user_image,\
    Text \
FROM twitter_raw;

-- Create a derived stream with user_screenname | category
CREATE STREAM category_tweets AS \
SELECT \
    user_screenname, \
    (Text LIKE '%drum%') AS IS_DRUM, \
    (Text LIKE '%melody%') AS IS_MELODY, \
    (Text like '%heavy%') AS IS_HEAVY_BASS, \
    (Text like '%lead%') AS IS_LEAD_BASS, \
    (Text like '%line%') AS IS_LINE_BASS, \
    (Text like '%pad%') AS IS_PAD, \
    (Text like '%synth%') AS IS_SYNTH, \
    (Text like '%vocal%') AS IS_VOCAL, \
    Text \
FROM twitter_clean;

-- Count tweets per user
CREATE TABLE user_tweet_count AS \
SELECT user_screenName, COUNT(*) AS count \
FROM twitter_clean \
GROUP BY user_screenname HAVING COUNT(*) > 1;

-- Join between user tweets and categories INNER IS NOT SUPPORTED
-- TODO : this doesn't work.
SELECT utc.screenname, utc.count, count(cat.is_drum) \
FROM category_tweets cat \
LEFT JOIN user_tweet_count utc ON cat.user_screenname = utc.user_screenName \
WHERE utc.count > 10;