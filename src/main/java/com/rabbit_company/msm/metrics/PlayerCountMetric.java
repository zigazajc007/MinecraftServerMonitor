package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerCountMetric implements MetricProvider {
    private final Plugin plugin;
    private final int interval;
    private BukkitTask sampleTask;

    private final ConcurrentHashMap<String, Integer> playerCount = new ConcurrentHashMap<>();

    private Method getPlayerCountMethod = null;
    private boolean isPaper = false;

    public PlayerCountMetric(Plugin plugin, int interval) {
        this.plugin = plugin;
        this.interval = interval;

        detectPaper();
    }

    private void detectPaper() {
        try {
            getPlayerCountMethod = World.class.getMethod("getPlayerCount");
            isPaper = true;
            plugin.getLogger().info("Detected Paper API - using World#getPlayerCount()");
        } catch (NoSuchMethodException ignored) {
            isPaper = false;
            plugin.getLogger().info("Paper API not found - falling back to getPlayers().size()");
        }
    }

    @Override
    public void start() {
        sampleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            playerCount.clear();

            for(World world : Bukkit.getWorlds()) {
                int count;

                if(isPaper) {
                    try {
                        count = (int) getPlayerCountMethod.invoke(world);
                    }catch (Exception e){
                        count = world.getPlayers().size();
                    }
                }else{
                    count = world.getPlayers().size();
                }

                playerCount.put(world.getName(), count);
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
