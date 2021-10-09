package com.github.robertzych.classifier;

public class MyRecord implements Comparable<MyRecord> {
    private String name;
    private Long count;
    private Long timestamp;

    public MyRecord(String name, Long count, Long timestamp) {
        this.name = name;
        this.count = count;
        this.timestamp = timestamp;
    }

    public String getName() {
        return name;
    }

    public Long getCount() {
        return count;
    }

    @Override
    public int compareTo(MyRecord other) {
        if (other.count != this.count)
            return (int) (other.count - this.count);

        return (int) (this.timestamp - other.timestamp);
    }
}
