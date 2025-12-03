package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;

public class PlayerCountMetric implements MetricProvider {
    private final Plugin plugin;
    private final int interval;
    private BukkitTask sampleTask;

    private final AtomicInteger count = new AtomicInteger(0);

    public PlayerCountMetric(Plugin plugin, int interval){
        this.plugin = plugin;
        this.interval = interval;
    }

    @Override
    public void start() {
        sampleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            count.set(Bukkit.getOnlinePlayers().size());
        }, 0L, 20L * this.interval);
    }

    @Override
    public void stop() {
        if (sampleTask != null) sampleTask.cancel();
    }

    @Override
    public String collect() {
        return
                "# HELP minecraft_players Current number of online players\n" +
                "# TYPE minecraft_players gauge\n" +
                String.format("minecraft_players %d\n", count.get());
    }
}
