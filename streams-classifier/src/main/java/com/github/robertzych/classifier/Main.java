package com.github.robertzych.classifier;

import com.beust.jcommander.JCommander;
import javax.swing.text.html.Option;

public class Main {
    public static void main(String[] args) {

        final Options options = new Options();

        final JCommander jCommander = JCommander.newBuilder()
            .addObject(options)
            .build();

        jCommander.parse(args);

        if (options.isHelp()) {
            jCommander.usage();
            return;
        }

        TweetClassifier tweetClassifier = new TweetClassifier();
        tweetClassifier.start(options);
    }
}
