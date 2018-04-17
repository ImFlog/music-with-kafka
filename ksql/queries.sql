-- Reset streams and tables
DROP STREAM twitter_foo;
DROP TABLE user_tweet_count;
DROP STREAM category_tweets;
DROP STREAM twitter_rekeyed;
DROP STREAM twitter_clean;
DROP STREAM twitter_raw;

-- Set properties
SET 'auto.offset.reset' = 'earliest';
SET 'commit.interval.ms' = '5000';

-- Read twitter stream
CREATE STREAM twitter_raw (CreatedAt BIGINT, Id BIGINT, Text VARCHAR, User VARCHAR) \
WITH (KAFKA_TOPIC='twitter_json', VALUE_FORMAT='JSON');

-- Show stream content
SELECT * FROM twitter_raw;
-- Stream is infinite by definition, CTRL+C to stop.

-- Create a derived stream with user values & text only
CREATE STREAM twitter_clean AS \
SELECT TIMESTAMPTOSTRING(CreatedAt, 'yyyy-MM-dd HH:mm:ss.SSS') AS CreatedAt,\
    EXTRACTJSONFIELD(User,'$.Name') AS Name,\
    EXTRACTJSONFIELD(User,'$.ScreenName') AS ScreenName,\
    EXTRACTJSONFIELD(User,'$.ProfileImageURL') AS Image,\
    Text \
FROM twitter_raw;

-- Clean version print but limit to end the query
SELECT * FROM twitter_clean LIMIT 10;

-- We can start to play, count tweets per user
CREATE TABLE user_tweet_count AS \
SELECT ScreenName, COUNT(*) AS Count \
FROM twitter_clean \
GROUP BY ScreenName;

-- Show data
SELECT ScreenName, Count FROM user_tweet_count LIMIT 10;

-- Join example : Melody tweets per user 
-- We need to force PARTITION BY https://github.com/confluentinc/ksql/issues/749
CREATE STREAM category_tweets AS \
SELECT \
    ScreenName, \
    (Text LIKE '%drum%') AS IsDrum, \
    (Text LIKE '%melody%') AS IsMelody, \
    (Text like '%heavy%') AS IsHeavyBass, \
    (Text like '%lead%') AS IsLeadBass, \
    (Text like '%line%') AS IsLineBass, \
    (Text like '%pad%') AS IsPad, \
    (Text like '%synth%') AS IsSynth, \
    (Text like '%vocal%') AS IsVocal, \
    Text \
FROM twitter_clean \
PARTITION BY ScreenName;

-- Actual JOIN query (Melody tweets per user)
SELECT \
    cat.ScreenName AS ScreenName, \
    Max(utc.Count) AS TotalCount, \
    COUNT(cat.isMelody) AS MelodyCount \
FROM category_tweets cat \
LEFT JOIN user_tweet_count utc ON cat.ScreenName = utc.ScreenName \
WHERE cat.IsMelody = true \
AND utc.Count > 5 \
GROUP BY cat.ScreenName;

-- RECREATE charts like processor
-- Workaround for mandatory GROUP BY : https://github.com/confluentinc/ksql/issues/430
CREATE STREAM twitter_foo AS \
SELECT 1 AS FOO, * FROM twitter_clean;

-- Default commit interval is 10sec
SELECT TIMESTAMPTOSTRING(ROWTIME, 'yyyy-MM-dd HH:mm:ss.SSS') AS WINDOW_START, ScreenName, COUNT(*) \
FROM twitter_foo \
WINDOW TUMBLING (SIZE 30 SECONDS) \
WHERE Text LIKE '%drum%' \
GROUP BY FOO, ScreenName;
