package fr.ippon.kafka.streams.domains.twitter;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@NoArgsConstructor
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
}