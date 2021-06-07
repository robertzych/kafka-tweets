package com.kafkatweets.twitterproducer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RawTweet {
    public String created_at;
    public String id_str;
    public String text;
}
