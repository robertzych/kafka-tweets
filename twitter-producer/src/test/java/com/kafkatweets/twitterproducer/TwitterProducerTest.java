package com.kafkatweets.twitterproducer;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class TwitterProducerTest {
    @Test
    public void convertJsonTweet(){
        String filePath = "./src/test/java/com/kafkatweets/twitterproducer/tweet.json";
        Path sampleTweet = Path.of(filePath);
        String jsonTweet = null;
        try {
            jsonTweet = Files.readString(sampleTweet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Tweet tweet = TwitterProducer.convertJsonTweet(jsonTweet);
        Assert.assertEquals("Mon May 17 12:21:09 +0000 2021", tweet.getCreatedAt());
        Assert.assertEquals("1394266738699735040", tweet.getIdStr());
        Assert.assertEquals("@PeterSchiff Remember when Elon crashed the price of $TSLA saying it was over valued + his appearance on the on Joe… https://t.co/V6xZ9TTGcD",
                tweet.getText());
    }
}