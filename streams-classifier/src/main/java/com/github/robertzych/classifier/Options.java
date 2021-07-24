package com.github.robertzych.classifier;

import lombok.Getter;

import java.util.UUID;

@Getter
public class Options {

    private String bootstrapServers = "localhost:9092";

    private String applicationId = "streams-classifier";

    private String clientId = "s-" + UUID.randomUUID();

    private String autoOffsetReset = "earliest";

    private String tweetsTopic = "twitter_json_01";
}
