package fr.ippon.kafka.streams.utils;

import fr.ippon.kafka.streams.serdes.pojos.TwitterStatus;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Audio {

    /**
     * Read audio directory and count the number of .ogg files in each directory.
     * The string will be used to split tweets.
     */
    public static Map<String, Integer> retrieveAvailableCategories() {
        Map<String, Integer> categoriesAndCount = new HashMap<>();
        File[] filesList = new File("../audio/").listFiles();
        if (filesList != null) {
            Arrays.stream(filesList).forEach(f -> {
                if (f.isDirectory()) {
                    categoriesAndCount.put(f.getName(), countSubAudioFiles(f));
                }
            });
        }
        return categoriesAndCount;
    }

    public static Integer countSubAudioFiles(File file) {
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
        for (Map.Entry<String, Integer> category : categories.entrySet()) {
            if (matchCategory(category, value)) {
                return category.getKey();
            }
        }
        return null;
    }

    /**
     * Check if it matches a category.
     * "_" and "-" are matched as space too.
     */
    public static boolean matchCategory(Map.Entry<String, Integer> category, TwitterStatus value) {
        String tweetText = value.getText().toLowerCase();
        return tweetText.contains(category.getKey()) ||
                tweetText.contains(category.getKey()
                        .replace("-", " ")
                        .replace("_", " "));
    }
}
