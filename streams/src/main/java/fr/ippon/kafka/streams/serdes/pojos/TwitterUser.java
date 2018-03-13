package fr.ippon.kafka.streams.serdes.pojos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class TwitterUser {
    private Long id;
    private String name;
    private String screenName;
    private String location;
    private String description;
    private boolean contributorsEnabled;
    private String profileImageURL;
    private boolean defaultProfileImage;
    private String URL;
    private int followersCount;
    private int friendsCount;
    private int favouritesCount;
    private int utcOffset;
    private String lang;
    private int statusesCount;
    private boolean geoEnabled;
    private boolean verified;
    private boolean translator;
    private int listedCount;
    private boolean followRequestSent;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isContributorsEnabled() {
        return contributorsEnabled;
    }

    public void setContributorsEnabled(boolean contributorsEnabled) {
        this.contributorsEnabled = contributorsEnabled;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    public boolean isDefaultProfileImage() {
        return defaultProfileImage;
    }

    public void setDefaultProfileImage(boolean defaultProfileImage) {
        this.defaultProfileImage = defaultProfileImage;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }

    public int getFavouritesCount() {
        return favouritesCount;
    }

    public void setFavouritesCount(int favouritesCount) {
        this.favouritesCount = favouritesCount;
    }

    public int getUtcOffset() {
        return utcOffset;
    }

    public void setUtcOffset(int utcOffset) {
        this.utcOffset = utcOffset;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public int getStatusesCount() {
        return statusesCount;
    }

    public void setStatusesCount(int statusesCount) {
        this.statusesCount = statusesCount;
    }

    public boolean isGeoEnabled() {
        return geoEnabled;
    }

    public void setGeoEnabled(boolean geoEnabled) {
        this.geoEnabled = geoEnabled;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isTranslator() {
        return translator;
    }

    public void setTranslator(boolean translator) {
        this.translator = translator;
    }

    public int getListedCount() {
        return listedCount;
    }

    public void setListedCount(int listedCount) {
        this.listedCount = listedCount;
    }

    public boolean isFollowRequestSent() {
        return followRequestSent;
    }

    public void setFollowRequestSent(boolean followRequestSent) {
        this.followRequestSent = followRequestSent;
    }

    @Override
    public String toString() {
        return "TwitterUser{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", screenName='" + screenName + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", contributorsEnabled=" + contributorsEnabled +
                ", profileImageURL='" + profileImageURL + '\'' +
                ", defaultProfileImage=" + defaultProfileImage +
                ", URL='" + URL + '\'' +
                ", followersCount=" + followersCount +
                ", friendsCount=" + friendsCount +
                ", favouritesCount=" + favouritesCount +
                ", utcOffset=" + utcOffset +
                ", lang='" + lang + '\'' +
                ", statusesCount=" + statusesCount +
                ", geoEnabled=" + geoEnabled +
                ", verified=" + verified +
                ", translator=" + translator +
                ", listedCount=" + listedCount +
                ", followRequestSent=" + followRequestSent +
                '}';
    }
}