package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LoadedEntitiesMetric implements MetricProvider {
    private final Plugin plugin;
    private final int interval;
    private final String countingMethod;
    private final boolean countTypes;
    private BukkitTask sampleTask;

    public static final ConcurrentHashMap<String, Integer> entitiesCounts = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> entityTypesCounts = new ConcurrentHashMap<>();

    public LoadedEntitiesMetric(Plugin plugin, int interval, String countingMethod, boolean countTypes) {
        this.plugin = plugin;
        this.interval = interval;
        this.countingMethod = countingMethod;
        this.countTypes = countTypes;
    }

    @Override
    public void start() {
        if (countingMethod.equalsIgnoreCase("event")){
            for (World world : Bukkit.getWorlds()) {
                if (!entitiesCounts.containsKey(world.getName())) entitiesCounts.put(world.getName(), 0);
            }
            return;
        }

        sampleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            entitiesCounts.clear();
            entityTypesCounts.clear();

            for (World world : Bukkit.getWorlds()) {
                if(countTypes){
                    ConcurrentHashMap<String, Integer> entityTypes = new ConcurrentHashMap<>();
                    for (Entity entity : world.getEntities()) {
                        entityTypes.compute(entity.getType().name(), (key, value) -> (value == null) ? 1 : value + 1);
                    }
                    entityTypesCounts.put(world.getName(), entityTypes);
                }else{
                    entitiesCounts.put(world.getName(), world.getEntityCount());
                }
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

        if(countTypes){
            for (Map.Entry<String, ConcurrentHashMap<String, Integer>> entry : entityTypesCounts.entrySet()) {
                String worldName = entry.getKey();
                ConcurrentHashMap<String, Integer> loadedEntities = entry.getValue();

                // Escape world name for Prometheus label (replace problematic characters)
                String escapedWorldName = worldName.replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\\", "\\\\");

                for (Map.Entry<String, Integer> entity : loadedEntities.entrySet()) {
                    builder.append(String.format(
                            "minecraft_loaded_entities{world=\"%s\",entity=\"%s\"} %d\n",
                            escapedWorldName,
                            entity.getKey(),
                            entity.getValue()
                    ));
                }
            }
        } else {
            for (Map.Entry<String, Integer> entry : entitiesCounts.entrySet()) {
                String worldName = entry.getKey();
                int loadedEntities = entry.getValue();

                // Escape world name for Prometheus label (replace problematic characters)
                String escapedWorldName = worldName.replace("\"", "\\\"")
                        .replace("\n", "\\n")
                        .replace("\\", "\\\\");

                builder.append(String.format(
                        "minecraft_loaded_entities{world=\"%s\"} %d\n",
                        escapedWorldName,
                        loadedEntities
                ));
            }
        }

        return builder.toString();
    }
}
