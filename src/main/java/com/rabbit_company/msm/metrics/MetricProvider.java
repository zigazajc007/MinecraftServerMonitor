package com.rabbit_company.msm.metrics;

public interface MetricProvider {
    void start();
    void stop();
    String collect();
    boolean isEnabled();
}
