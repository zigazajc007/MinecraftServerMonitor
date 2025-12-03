package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TpsMetric implements MetricProvider {
    private final Plugin plugin;
    private final int interval;
    private final Queue<Long> tickTimestamps = new ConcurrentLinkedQueue<>();
    private static final int MAX_HISTORY_SIZE = 18000; // 15 minutes

    private volatile double tps1m = 20.0;
    private volatile double tps5m = 20.0;
    private volatile double tps15m = 20.0;

    private final long startTime = System.currentTimeMillis();

    private BukkitTask collectTask;
    private BukkitTask calculateTask;

    public TpsMetric(Plugin plugin, int interval){
        this.plugin = plugin;
        this.interval = interval;
    }

    @Override
    public void start() {
        collectTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            tickTimestamps.offer(System.currentTimeMillis());

            while (tickTimestamps.size() > MAX_HISTORY_SIZE) {
                tickTimestamps.poll();
            }
        }, 1L, 1L);

        calculateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::calculateTPS, 100L, 20L * this.interval);
    }

    private void calculateTPS() {
        long currentTime = System.currentTimeMillis();
        long oneMinuteAgo = currentTime - 60000L;
        long fiveMinutesAgo = currentTime - 300000L;
        long fifteenMinutesAgo = currentTime - 900000L;

        int ticks1m = 0;
        int ticks5m = 0;
        int ticks15m = 0;

        // Count ticks in each time window
        for (long timestamp : tickTimestamps) {
            if (timestamp >= oneMinuteAgo) ticks1m++;
            if (timestamp >= fiveMinutesAgo) ticks5m++;
            if (timestamp >= fifteenMinutesAgo) ticks15m++;
        }

        tps1m = Math.min(20.0, ticks1m / 60.0);
        tps5m = Math.min(20.0, ticks5m / 300.0);
        tps15m = Math.min(20.0, ticks15m / 900.0);
    }

    @Override
    public void stop() {
        if (collectTask != null) collectTask.cancel();
        if (calculateTask != null) calculateTask.cancel();
    }

    @Override
    public String collect() {
        StringBuilder sb = new StringBuilder();
        sb.append("# HELP minecraft_tps Server ticks per second\n").append("# TYPE minecraft_tps gauge\n");

        long uptime = System.currentTimeMillis() - startTime;

        if (uptime >= 60000) {
            sb.append(String.format("minecraft_tps{interval=\"1m\"} %.2f\n", tps1m));
        }
        if (uptime >= 300000) {
            sb.append(String.format("minecraft_tps{interval=\"5m\"} %.2f\n", tps5m));
        }
        if (uptime >= 900000) {
            sb.append(String.format("minecraft_tps{interval=\"15m\"} %.2f\n", tps15m));
        }

        return sb.toString();
    }
}
