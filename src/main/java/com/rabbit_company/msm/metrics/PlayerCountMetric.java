package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCountMetric implements MetricProvider {
    private final Plugin plugin;
    private boolean enabled = false;
    private BukkitTask sampleTask;

    public static final ConcurrentHashMap<String, Integer> playerCount = new ConcurrentHashMap<>();

    public PlayerCountMetric(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        if(!plugin.getConfig().getBoolean("metrics.player_count.enabled", true)) return;

        int interval = plugin.getConfig().getInt("metrics.player_count.interval", 1);

        sampleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            playerCount.clear();

            for(World world : Bukkit.getWorlds()) {
                playerCount.put(world.getName(), world.getPlayerCount());
            }
        }, 0L, 20L * interval);

        enabled = true;
    }

    @Override
    public void stop() {
        enabled = false;
        if (sampleTask != null) sampleTask.cancel();
        sampleTask = null;
        playerCount.clear();
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

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
