package com.kafkatweets.twitterproducer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Client;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.OAuth1;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class TwitterProducer {

    static Logger logger = LoggerFactory.getLogger(TwitterProducer.class.getName());

    List<String> terms = Lists.newArrayList("kafka");

    public TwitterProducer() {
    }

    public static void main(String[] args) {
        new TwitterProducer().run();
    }

    public void run() {
        logger.info("Setup");
        BlockingQueue<String> msgQueue = new LinkedBlockingDeque<>(1000);

        // create a twitter client
        Client client = createTwitterClient(msgQueue);
        client.connect();

        // create a Kafka producer
        KafkaProducer<String, Tweet> producer = createKafkaProducer();

        // add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("stopping application...");
            logger.info("shutting down client from twitter...");
            client.stop();
            logger.info("closing producer...");
            producer.close();
            logger.info("done!");
        }));

        // loop to send tweets to Kafka
        while (!client.isDone()) {
            String jsonTweet = null;
            try {
                jsonTweet = msgQueue.poll(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
                client.stop();
            }
            if (jsonTweet != null) {
                logger.info(jsonTweet);
                Tweet tweet = convertJsonTweet(jsonTweet);
                producer.send(new ProducerRecord<>("twitter_tweets_avro", null, tweet),
                        (metadata, exception) -> {
                            if (exception != null) {
                                logger.error("Something bad happened", exception);
                            }
                        });
            }
        }
        logger.info("End of application");

    }

    public static Tweet convertJsonTweet(String jsonTweet) {
        ObjectMapper objectMapper = new ObjectMapper();
        RawTweet rawTweet = null;
        try {
            rawTweet = objectMapper.readValue(jsonTweet, RawTweet.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Failed to map json to rawTweet", e);
        }
        Tweet tweet = null;
        if (rawTweet != null) {
            tweet = new Tweet(rawTweet.created_at, rawTweet.id_str, rawTweet.text);
        }
        return tweet;
    }

    private KafkaProducer<String, Tweet> createKafkaProducer() {
        String bootstrapServers = "127.0.0.1:9092";
        String schemaRegistryServer = "http://localhost:8081";

        // create Producer properties
        Properties properties = new Properties();
        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class.getName());
        properties.setProperty("schema.registry.url", schemaRegistryServer);

        // create the producer
        return new KafkaProducer<>(properties);
    }

    public Client createTwitterClient(BlockingQueue<String> msgQueue) {
        Hosts hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
        StatusesFilterEndpoint hosebirdEndpoint = new StatusesFilterEndpoint();
        // Optional: set up some followings and track terms
        // List<Long> followings = Lists.newArrayList(1234L, 566788L);
        // hosebirdEndpoint.followings(followings);
        hosebirdEndpoint.trackTerms(terms);


        String credentialFile = "./credentials/twitter.txt";
        String consumerKey = null;
        String consumerSecret = null;
        String token = null;
        String secret = null;
        try {
            if (Files.exists(Paths.get(credentialFile))) {
                List<String> lines = Files.readAllLines(
                        Paths.get(credentialFile),
                        Charset.defaultCharset());
                consumerKey = lines.get(0);
                consumerSecret = lines.get(1);
                token = lines.get(2);
                secret = lines.get(3);
            }
        } catch (IOException e) {
            logger.error("Could not read twitter credentials from file");
        }

        if (consumerKey == null || consumerSecret == null || token == null || secret == null) {
            return null;
        }

        // These secrets should be read from a config file
        Authentication hosebirdAuth = new OAuth1(consumerKey, consumerSecret, token, secret);

        ClientBuilder builder = new ClientBuilder()
                .name("Hosebird-Client-01")                              // optional: mainly for the logs
                .hosts(hosebirdHosts)
                .authentication(hosebirdAuth)
                .endpoint(hosebirdEndpoint)
                .processor(new StringDelimitedProcessor(msgQueue));

        return builder.build();
    }
}
