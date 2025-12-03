package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoadedChunksMetric implements MetricProvider {
    private final Plugin plugin;
    private final int interval;
    private BukkitTask sampleTask;

    private final ConcurrentHashMap<String, Integer> worldChunkCounts = new ConcurrentHashMap<>();

    private Method getChunkCountMethod = null;
    private boolean isPaper = false;

    public LoadedChunksMetric(Plugin plugin, int interval) {
        this.plugin = plugin;
        this.interval = interval;

        detectPaper();
    }

    private void detectPaper() {
        try {
            getChunkCountMethod = World.class.getMethod("getChunkCount");
            isPaper = true;
            plugin.getLogger().info("Detected Paper API - using World#getChunkCount()");
        } catch (NoSuchMethodException ignored) {
            isPaper = false;
            plugin.getLogger().info("Paper API not found - falling back to getLoadedChunks().length");
        }
    }

    @Override
    public void start() {
        sampleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            worldChunkCounts.clear();

            for (World world : Bukkit.getWorlds()) {
                int count;

                if (isPaper) {
                    try {
                        count = (int) getChunkCountMethod.invoke(world);
                    } catch (Exception e) {
                        count = world.getLoadedChunks().length;
                    }
                } else {
                    count = world.getLoadedChunks().length;
                }

                worldChunkCounts.put(world.getName(), count);
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

        builder.append("# HELP minecraft_loaded_chunks Current number of loaded chunks per world\n");
        builder.append("# TYPE minecraft_loaded_chunks gauge\n");

        for (Map.Entry<String, Integer> entry : worldChunkCounts.entrySet()) {
            String worldName = entry.getKey();
            int loadedChunks = entry.getValue();

            // Escape world name for Prometheus label (replace problematic characters)
            String escapedWorldName = worldName.replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\\", "\\\\");

            builder.append(String.format(
                    "minecraft_loaded_chunks{world=\"%s\"} %d\n",
                    escapedWorldName,
                    loadedChunks
            ));
        }

        return builder.toString();
    }
}
