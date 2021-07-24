package com.github.robertzych.classifier;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.KStream;

import java.security.cert.CertPathBuilder;
import java.util.Map;
import java.util.Properties;

@Slf4j
public class TweetClassifier {

    private Map<String, Object> properties(final Options options) {
        final Map<String, Object> defaults = Map.ofEntries(
                Map.entry(ProducerConfig.LINGER_MS_CONFIG, 100),
                Map.entry(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, options.getBootstrapServers()),
                Map.entry(StreamsConfig.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT"),
                Map.entry(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName()),
                Map.entry(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, JsonSerde.class.getName()),
                Map.entry(StreamsConfig.APPLICATION_ID_CONFIG, options.getApplicationId()),
                Map.entry(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, options.getAutoOffsetReset()),
                Map.entry(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true"),
                Map.entry(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 100),
                Map.entry(StreamsConfig.CLIENT_ID_CONFIG, options.getClientId()),
                Map.entry(StreamsConfig.TOPOLOGY_OPTIMIZATION_CONFIG, StreamsConfig.OPTIMIZE),
                Map.entry(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class)
        );
        return defaults;
    }

    public void start() {

        Options options = new Options();
        Properties p = toProperties(properties(options));

        log.info("starting streams: " + options.getClientId());

        final Topology topology = streamsBuilder(options).build(p);

        log.info("Topology:\n" + topology.describe());

        final KafkaStreams streams = new KafkaStreams(topology, p);

        // TODO: streams.setUncaughtExceptionHandler(?)

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private StreamsBuilder streamsBuilder(final Options options) {

        final StreamsBuilder builder = new StreamsBuilder();

        KStream<String, JsonNode> tweets = builder.stream(options.getTweetsTopic());

        return builder;
    }

    private static Properties toProperties(final Map<String, Object> map) {
        final Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }
}
