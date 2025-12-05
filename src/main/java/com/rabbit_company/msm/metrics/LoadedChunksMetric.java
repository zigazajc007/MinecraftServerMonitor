package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoadedChunksMetric implements MetricProvider {
    private final Plugin plugin;
    private final int interval;
    private final String countingMethod;
    private BukkitTask sampleTask;

    public static final ConcurrentHashMap<String, Integer> worldChunkCounts = new ConcurrentHashMap<>();

    private MethodHandle getChunkCountHandle = null;
    private boolean isPaper = false;

    public LoadedChunksMetric(Plugin plugin, int interval, String countingMethod) {
        this.plugin = plugin;
        this.interval = interval;
        this.countingMethod = countingMethod;

        if (!countingMethod.equalsIgnoreCase("event")) detectPaper();
    }

    private void detectPaper() {
        try {
            Method method = World.class.getMethod("getChunkCount");
            method.setAccessible(true);

            getChunkCountHandle = MethodHandles.lookup().unreflect(method);
            isPaper = true;
            plugin.getLogger().info("Detected Paper API - using World#getChunkCount()");
        } catch (Throwable ignored) {
            isPaper = false;
            plugin.getLogger().info("Paper API not found - falling back to getLoadedChunks().length");
        }
    }

    private int getChunkCount(World world) {
        if (!isPaper) return world.getLoadedChunks().length;

        try {
            return (int) getChunkCountHandle.invoke(world);
        } catch (Throwable ignored) {
            return world.getLoadedChunks().length;
        }
    }

    @Override
    public void start() {
        if (countingMethod.equalsIgnoreCase("event")){
            for (World world : Bukkit.getWorlds()) {
                if (!worldChunkCounts.containsKey(world.getName())) worldChunkCounts.put(world.getName(), 0);
            }
            return;
        }

        sampleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            worldChunkCounts.clear();

            for (World world : Bukkit.getWorlds()) {
                worldChunkCounts.put(world.getName(), getChunkCount(world));
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
