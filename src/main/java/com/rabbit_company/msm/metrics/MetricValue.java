package com.rabbit_company.msm.metrics;

public class MetricValue {
    private final String value;
    private final long timestamp;

    public MetricValue(String value, long timestamp) {
        this.value = value;
        this.timestamp = timestamp;
    }

    public String value() {
        return value;
    }

    public long timestamp() {
        return timestamp;
    }
}