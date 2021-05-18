package com.kafkatweets.twitterproducer;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.*;

public class TwitterProducerTest {
    @Test
    public void TestSomething(){
        TwitterProducer twitterProducer = new TwitterProducer();
        Assert.assertEquals(1, 1);
    }
}