package fr.ippon.kafka.streams.utils;

import fr.ippon.kafka.streams.domains.category.Categories;
import fr.ippon.kafka.streams.domains.category.CategoriesCollector;
import fr.ippon.kafka.streams.domains.twitter.TwitterStatus;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;

public final class Audio {

    public static final String UNKNOW = "unknow";
    private static final String DASH_SEPARATOR = "-";
    private static final String UNDERSCORE_SEPARATOR = "_";
    private static final String SPACE_SEPARATOR = " ";

    /**
     * Read audio directory and count the number of .ogg files in each directory.
     * The string will be used to split tweets.
     */
    public static Categories retrieveAvailableCategories() {
        File[] filesList = new File("../audio/").listFiles();
        return Optional
                .ofNullable(filesList)
                .map(files -> Arrays
                        .stream(files)
                        .filter(File::isDirectory)
                        .collect(new CategoriesCollector(Audio::countSubAudioFiles))
                )
                .orElseGet(Categories::new);
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
     * @return The category key if found. Unknow if not
     */
    public static String findCategory(TwitterStatus value, Categories categories) {
        return categories
                .toStream()
                .filter(category -> matchCategory(category, value))
                .findFirst()
                .orElse(UNKNOW);
    }

    /**
     * Check if it matches a category.
     * "_" and "-" are matched as space too.
     */
    private static boolean matchCategory(String category, TwitterStatus value) {
        String tweetText = value.getText().toLowerCase();
        return tweetText.contains(category) ||
                tweetText.contains(category
                        .replace(DASH_SEPARATOR, SPACE_SEPARATOR)
                        .replace(UNDERSCORE_SEPARATOR, SPACE_SEPARATOR));
    }
}
