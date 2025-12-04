package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class MemoryMetric implements MetricProvider {
    private final Plugin plugin;
    private final int interval;
    private BukkitTask sampleTask;

    private volatile long usedMemory = 0;
    private volatile long totalMemory = 0;
    private volatile long maxMemory = 0;

    public MemoryMetric(Plugin plugin, int interval) {
        this.plugin = plugin;
        this.interval = interval;
    }

    @Override
    public void start() {
        sampleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            totalMemory = Runtime.getRuntime().totalMemory();
            maxMemory = Runtime.getRuntime().maxMemory();
        }, 0L, 20L * this.interval);
    }

    @Override
    public void stop() {
        if (sampleTask != null ) sampleTask.cancel();
    }

    @Override
    public String collect() {
        return "# HELP minecraft_memory_used_bytes Used memory in bytes\n" +
                "# TYPE minecraft_memory_used_bytes gauge\n" +
                "# UNIT minecraft_memory_used_bytes bytes\n" +
                String.format("minecraft_memory_used_bytes %d\n", usedMemory) +

                "# HELP minecraft_memory_total_bytes Total memory in bytes\n" +
                "# TYPE minecraft_memory_total_bytes gauge\n" +
                "# UNIT minecraft_memory_total_bytes bytes\n" +
                String.format("minecraft_memory_total_bytes %d\n", totalMemory) +

                "# HELP minecraft_memory_max_bytes Max memory in bytes\n" +
                "# TYPE minecraft_memory_max_bytes gauge\n" +
                "# UNIT minecraft_memory_max_bytes bytes\n" +
                String.format("minecraft_memory_max_bytes %d\n", maxMemory);
    }
}
