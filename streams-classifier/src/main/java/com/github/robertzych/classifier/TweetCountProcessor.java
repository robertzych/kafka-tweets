package com.github.robertzych.classifier;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

import java.time.Duration;

@Slf4j
public class TweetCountProcessor implements Processor<String, Long> {

    private final String storeName;
    private ProcessorContext context;
    private KeyValueStore<String, Long> kvStore;

    public TweetCountProcessor(String storeName) {
        this.storeName = storeName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext processorContext) {
        context = processorContext;
        kvStore = context.getStateStore(storeName);

        this.context.schedule(Duration.ofMillis(1000), PunctuationType.WALL_CLOCK_TIME, (timestamp) -> {
            KeyValueIterator<String, Long> iter = kvStore.all();
            while (iter.hasNext()) {
                KeyValue<String, Long> entry = iter.next();
                log.info("key={},value={}", entry.key, entry.value);
                context.forward(entry.key, entry.value.toString());
            }
            iter.close();

            // commit the current processing progress
            context.commit();
        });
    }

    @Override
    public void process(String s, Long aLong) {
        // intentionally left blank
    }

    @Override
    public void close() {
        // intentionally left blank
    }
}
