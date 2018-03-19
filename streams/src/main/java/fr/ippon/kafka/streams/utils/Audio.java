package fr.ippon.kafka.streams.utils;

import fr.ippon.kafka.streams.serdes.pojos.TwitterStatus;

import java.io.File;
import java.util.*;

import static java.util.stream.Collectors.toMap;

public class Audio {

    /**
     * Read audio directory and count the number of .ogg files in each directory.
     * The string will be used to split tweets.
     */
    public static Map<String, Integer> retrieveAvailableCategories() {
        File[] filesList = new File("../audio/").listFiles();
        return Optional
                .ofNullable(filesList)
                .map(fileList -> Arrays.stream(fileList).filter(File::isDirectory).collect(toMap(File::getName, Audio::countSubAudioFiles)))
                .orElse(Collections.emptyMap());
    }

    private static Integer countSubAudioFiles(File file) {
        File[] subFiles = file.listFiles();
        if (subFiles == null) {
            return 0;
        }
        return subFiles.length - 1;
    }

    /**
     * Finds the category associated to a tweet
     *
     * @param value      Tweet
     * @param categories Map of available categories
     * @return The category key if found. Null if not
     */
    public static String findCategory(TwitterStatus value, Map<String, Integer> categories) {
        return categories.entrySet().stream()
                .filter(entry -> matchCategory(entry, value))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    /**
     * Check if it matches a category.
     * "_" and "-" are matched as space too.
     */
    private static boolean matchCategory(Map.Entry<String, Integer> category, TwitterStatus value) {
        String tweetText = value.getText().toLowerCase();
        return tweetText.contains(category.getKey()) ||
                tweetText.contains(category.getKey()
                        .replace("-", " ")
                        .replace("_", " "));
    }
}
