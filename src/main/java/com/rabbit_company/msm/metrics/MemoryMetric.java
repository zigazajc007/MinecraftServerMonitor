package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class MemoryMetric implements MetricProvider {
    private final Plugin plugin;
    private boolean enabled = false;
    private BukkitTask sampleTask;

    private volatile long usedMemory = 0;
    private volatile long totalMemory = 0;
    private volatile long maxMemory = 0;

    public MemoryMetric(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        if(!plugin.getConfig().getBoolean("metrics.memory.enabled", true)) return;

        int interval = plugin.getConfig().getInt("metrics.memory.interval", 1);

        sampleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            usedMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
            totalMemory = Runtime.getRuntime().totalMemory();
            maxMemory = Runtime.getRuntime().maxMemory();
        }, 0L, 20L * interval);

        enabled = true;
    }

    @Override
    public void stop() {
        enabled = false;
        if (sampleTask != null ) sampleTask.cancel();
        sampleTask = null;
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

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
