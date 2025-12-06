package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCountMetric implements MetricProvider {
    private final Plugin plugin;
    private final int interval;
    private BukkitTask sampleTask;

    public static final ConcurrentHashMap<String, Integer> playerCount = new ConcurrentHashMap<>();

    public PlayerCountMetric(Plugin plugin, int interval) {
        this.plugin = plugin;
        this.interval = interval;
    }

    @Override
    public void start() {
        sampleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            playerCount.clear();

            for(World world : Bukkit.getWorlds()) {
                playerCount.put(world.getName(), world.getPlayerCount());
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

        builder.append("# HELP minecraft_players Current number of online players per world\n");
        builder.append("# TYPE minecraft_players gauge\n");

        for (Map.Entry<String, Integer> entry : playerCount.entrySet()) {
            String worldName = entry.getKey();
            int players = entry.getValue();

            // Escape world name for Prometheus label (replace problematic characters)
            String escapedWorldName = worldName.replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\\", "\\\\");

            builder.append(String.format(
                    "minecraft_players{world=\"%s\"} %d\n",
                    escapedWorldName,
                    players
            ));
        }

        return builder.toString();
    }
}
