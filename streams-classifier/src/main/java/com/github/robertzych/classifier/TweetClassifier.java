package com.github.robertzych.classifier;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import hex.genmodel.ModelMojoReader;
import hex.genmodel.MojoModel;
import hex.genmodel.MojoReaderBackend;
import hex.genmodel.MojoReaderBackendFactory;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

@Slf4j
public class TweetClassifier {

    private Map<String, Object> properties(final Options options) {
        final Map<String, Object> defaults = Map.ofEntries(
                Map.entry(ProducerConfig.LINGER_MS_CONFIG, 100),
                Map.entry(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, options.getBootstrapServers()),
                Map.entry(StreamsConfig.SECURITY_PROTOCOL_CONFIG, "PLAINTEXT"),
                Map.entry(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName()),
                Map.entry(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName()),
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

    public void start(final Options options) throws IOException {

        Properties p = toProperties(properties(options));

        log.info("starting streams: " + options.getClientId());

        final Topology topology = streamsBuilder(options).build(p);

        log.info("Topology:\n" + topology.describe());

        final KafkaStreams streams = new KafkaStreams(topology, p);

        // TODO: streams.setUncaughtExceptionHandler(?)

        streams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
    }

    private StreamsBuilder streamsBuilder(final Options options) throws IOException {

        JsonSerde jsonSerde = new JsonSerde();

        final StreamsBuilder builder = new StreamsBuilder();

        EasyPredictModelWrapper w2vModelWrapper = getModelWrapper("w2v_hex.zip");

        EasyPredictModelWrapper modelWrapper = getModelWrapper("DeepLearning_grid__1_AutoML_20210720_045710_model_2.zip");

        Materialized<String, Long, KeyValueStore<Bytes, byte[]>> materialized = Materialized.with(Serdes.String(), Serdes.Long());
        materialized.withCachingDisabled();
        builder
                .<String, JsonNode>stream(options.getTweetsTopic(), Consumed.with(Serdes.String(), jsonSerde))
                .peek((k, v) -> log.info("key={}, value={}", k, v))
                .map((k, v) -> {
                    String newKey = v.at("/User/ScreenName").asText();
                    String text = v.get("Text").asText();
                    String[] words = tokenize(text);
                    try {
                        float[] vectors = w2vModelWrapper.predictWord2Vec(words);
                        RowData row = new RowData();
                        for (int i = 0; i < 100; i++) {
                            row.put(String.format("C%s", (i + 1)), String.valueOf(vectors[i]));
                        }
                        MultinomialModelPrediction prediction = (MultinomialModelPrediction) modelWrapper.predict(row);
                        log.info("Community={}", prediction.label);
                        ObjectNode objectNode = v.deepCopy();
                        objectNode.put("community", prediction.label);
                        v = objectNode;
                    } catch (PredictException e) {
                        log.error(e.getMessage());
                    }
                    return new KeyValue<>(newKey, v);
                })
                .filter((k, v) -> v.get("community").asText().equals("apache kafka"))
                .groupByKey()
                .aggregate(() -> 0L, (k, v, a) -> a + 1, materialized)
                .toStream()
                .filter((k, v) -> v == 1L)
                .peek((k, v) -> log.info("ScreenName={}", k))
                .to(options.getUsersTopic());

        return builder;
    }

    private EasyPredictModelWrapper getModelWrapper(String modelPackageFileName) throws IOException {
        URL mojoSource = getClass().getClassLoader().getResource(modelPackageFileName);
        MojoReaderBackend reader = MojoReaderBackendFactory.createReaderBackend(mojoSource, MojoReaderBackendFactory.CachingStrategy.MEMORY);
        MojoModel model = ModelMojoReader.readFrom(reader);
        return new EasyPredictModelWrapper(model);
    }

    private static Properties toProperties(final Map<String, Object> map) {
        final Properties properties = new Properties();
        properties.putAll(map);
        return properties;
    }

    private static String[] tokenize(String text) {
        Set<String> stopWords = Set.of("ax", "i", "you", "edu", "s", "t", "m", "subject", "can", "lines", "re", "what", "there", "all", "we", "one", "the", "a", "an", "of", "or", "in", "for", "by", "on", "but", "is", "not", "with", "as", "was", "if", "they", "are", "this", "and", "it", "have", "from", "at", "my", "be", "that", "to", "com", "org", "like", "likes", "so");
        String[] words = text.toLowerCase().split("\\W+");
        return Arrays.stream(words)
                .filter(word -> word.length() >= 2)
                .filter(word -> !stopWords.contains(word))
                .toArray(String[]::new);
    }
}
