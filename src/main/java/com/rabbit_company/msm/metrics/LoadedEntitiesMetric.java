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
    private boolean enabled = false;
    private BukkitTask sampleTask;

    private boolean countTypes = false;

    public static final ConcurrentHashMap<String, Integer> entitiesCounts = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, ConcurrentHashMap<String, Integer>> entityTypesCounts = new ConcurrentHashMap<>();

    public LoadedEntitiesMetric(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        if(!plugin.getConfig().getBoolean("metrics.loaded_entities.enabled", true)) return;

        int interval = plugin.getConfig().getInt("metrics.loaded_entities.interval", 1);
        String countingMethod = plugin.getConfig().getString("metrics.loaded_entities.counting_method", "sampling");
        countTypes = plugin.getConfig().getBoolean("metrics.loaded_entities.count_types", false);

        if (countingMethod.equalsIgnoreCase("event")){
            for (World world : Bukkit.getWorlds()) {
                if (!entitiesCounts.containsKey(world.getName())) entitiesCounts.put(world.getName(), 0);
            }
            enabled = true;
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
        }, 0L, 20L * interval);

        enabled = true;
    }

    @Override
    public void stop() {
        enabled = false;
        if (sampleTask != null) sampleTask.cancel();
        sampleTask = null;
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

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
