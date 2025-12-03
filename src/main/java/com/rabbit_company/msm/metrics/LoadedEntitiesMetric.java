package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoadedEntitiesMetric implements MetricProvider {
    private final Plugin plugin;
    private final int interval;
    private BukkitTask sampleTask;

    private final ConcurrentHashMap<String, Integer> entitiesCounts = new ConcurrentHashMap<>();

    public LoadedEntitiesMetric(Plugin plugin, int interval) {
        this.plugin = plugin;
        this.interval = interval;
    }

    @Override
    public void start() {
        sampleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            entitiesCounts.clear();

            for (World world : Bukkit.getWorlds()) {
                entitiesCounts.put(world.getName(), world.getEntities().size());
            }
        }, 0L, 20L * this.interval);
    }

    @Override
    public void stop() {
        if (sampleTask != null) sampleTask.cancel();
    }

    @Override
    public String collect() {
        StringBuilder builder = new StringBuilder();

        builder.append("# HELP minecraft_loaded_entities Current number of loaded entities per world\n");
        builder.append("# TYPE minecraft_loaded_entities gauge\n");

        for (Map.Entry<String, Integer> entry : entitiesCounts.entrySet()) {
            String worldName = entry.getKey();
            int loadedChunks = entry.getValue();

            // Escape world name for Prometheus label (replace problematic characters)
            String escapedWorldName = worldName.replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\\", "\\\\");

            builder.append(String.format(
                    "minecraft_loaded_entities{world=\"%s\"} %d\n",
                    escapedWorldName,
                    loadedChunks
            ));
        }

        return builder.toString();
    }
}
