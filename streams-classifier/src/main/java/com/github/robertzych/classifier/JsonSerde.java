package com.github.robertzych.classifier;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

import java.util.Map;

public class JsonSerde implements Serde<JsonNode> {


    public JsonSerde() {
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public Serializer<JsonNode> serializer() {
        return new JsonSerializer<>();
    }

    @Override
    public Deserializer<JsonNode> deserializer() {
        return new JsonDeserializer<>();
    }
}
