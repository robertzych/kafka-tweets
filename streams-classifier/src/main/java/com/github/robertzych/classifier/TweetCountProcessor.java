package com.github.robertzych.classifier;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.processor.PunctuationType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.ValueAndTimestamp;

import java.time.Duration;
import java.util.*;


@Slf4j
public class TweetCountProcessor implements Processor<String, Long> {

    private final String storeName;
    private ProcessorContext context;
    private KeyValueStore<String, ValueAndTimestamp<Long>> kvStore;

    public TweetCountProcessor(String storeName) {
        this.storeName = storeName;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext processorContext) {
        context = processorContext;
        kvStore = context.getStateStore(storeName);
        int intervalMs = getIntervalMs();
        this.context.schedule(Duration.ofMillis(intervalMs), PunctuationType.WALL_CLOCK_TIME, (timestamp) -> {
            Map<String, MyRecord> map = new HashMap<>();

            KeyValueIterator<String, ValueAndTimestamp<Long>> iterator = kvStore.all();
            while (iterator.hasNext()) {
                KeyValue<String, ValueAndTimestamp<Long>> entry = iterator.next();
                map.put(entry.key, new MyRecord(entry.key,
                                                entry.value.value(),
                                                entry.value.timestamp()));
            }
            iterator.close();

            List<MyRecord> recordList = new ArrayList<>(map.values());
            Collections.sort(recordList);
            int rank = 1;
            int topN = 30;
            for (MyRecord entry: recordList.subList(0, topN)) {
                log.info("rank={},key={},value={}", rank, entry.getName(), entry.getCount());
                rank++;
            }

            // commit the current processing progress
            context.commit();
        });
    }

    private int getIntervalMs() {
        int intervalDays = 7;
        int intervalHours = intervalDays * 24;
        int intervalMinutes = intervalHours * 60;
        intervalMinutes = 5;
        int intervalSecs = intervalMinutes * 60;
        return intervalSecs * 1000;
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
