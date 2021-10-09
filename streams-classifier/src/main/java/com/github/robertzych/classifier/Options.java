package com.github.robertzych.classifier;

import com.beust.jcommander.Parameter;
import lombok.Getter;

import java.util.UUID;

@Getter
public class Options {

    @Parameter(names = "--help", help = true, hidden = true)
    private boolean help;

    @Parameter(names = "--bootstrap-servers")
    private String bootstrapServers = "localhost:9092";

    private String applicationId = "streams-classifier"; // + UUID.randomUUID();  // TODO: remove before deployment

    private String clientId = "s-" + UUID.randomUUID();

    private String autoOffsetReset = "earliest";

    @Parameter(names = { "--topic", "-t" })
    private String tweetsTopic = "twitter_json_01";

    private String usersTopic = "kafka_users";

    @Parameter(names = "--config-file")
    private String configFile = null;
}
