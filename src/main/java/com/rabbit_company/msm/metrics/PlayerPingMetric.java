package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class PlayerPingMetric implements MetricProvider {
    private final Plugin plugin;
    private boolean enabled = false;
    private BukkitTask sampleTask;

    private volatile double minPing = 0;
    private volatile double avgPing = 0;
    private volatile double maxPing = 0;

    public PlayerPingMetric(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void start() {
        if(!plugin.getConfig().getBoolean("metrics.player_ping.enabled", true)) return;

        int interval = plugin.getConfig().getInt("metrics.player_ping.interval", 1);

        sampleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
            int count = players.length;

            if(count == 0){
                minPing = 0;
                avgPing = 0;
                maxPing = 0;
            }else{
                double min = Double.MAX_VALUE;
                double max = 0;
                long sum = 0;

                for (Player p : players){
                    int ping = p.getPing();
                    sum += ping;
                    if (ping < min) min = ping;
                    if (ping > max) max = ping;
                }

                minPing = min;
                avgPing = sum / (double) count;
                maxPing = max;
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
        return "# HELP minecraft_min_player_ping_seconds Minimum player ping in seconds\n" +
                "# TYPE minecraft_min_player_ping_seconds gauge\n" +
                "# UNIT minecraft_min_player_ping_seconds seconds\n" +
                String.format("minecraft_min_player_ping_seconds %.3f\n", minPing / 1000.0) +

                "# HELP minecraft_avg_player_ping_seconds Average player ping in seconds\n" +
                "# TYPE minecraft_avg_player_ping_seconds gauge\n" +
                "# UNIT minecraft_avg_player_ping_seconds seconds\n" +
                String.format("minecraft_avg_player_ping_seconds %.3f\n", avgPing / 1000.0) +

                "# HELP minecraft_max_player_ping_seconds Maximum player ping in seconds\n" +
                "# TYPE minecraft_max_player_ping_seconds gauge\n" +
                "# UNIT minecraft_max_player_ping_seconds seconds\n" +
                String.format("minecraft_max_player_ping_seconds %.3f\n", maxPing / 1000.0);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
