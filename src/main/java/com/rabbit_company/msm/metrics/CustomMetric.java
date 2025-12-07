package com.rabbit_company.msm.metrics;

import java.util.concurrent.ConcurrentHashMap;

public class CustomMetric {
    private final String name;
    private final String description;
    private final String type;
    private final String placeholder;
    private final Integer interval;
    private final boolean perPlayer;

    private volatile MetricValue globalValue;
    private final ConcurrentHashMap<String, MetricValue> playerValues = new ConcurrentHashMap<>();

    public CustomMetric(String name, String description, String type, String placeholder, Integer interval, boolean perPlayer) {
        this.name = name;
        this.description = description;
        this.type = type;
        this.placeholder = placeholder;
        this.interval = interval;
        this.perPlayer = perPlayer;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public Integer getInterval() {
        return interval;
    }

    public boolean isPerPlayer() {
        return perPlayer;
    }

    public MetricValue getGlobalValue() {
        return globalValue;
    }

    public void setGlobalValue(String value) {
        this.globalValue = new MetricValue(value, System.currentTimeMillis());
    }

    public void setPlayerValue(String username, String value) {
        playerValues.put(username, new MetricValue(value, System.currentTimeMillis()));
    }

    public ConcurrentHashMap<String, MetricValue> getPlayerValues() {
        return playerValues;
    }
}
