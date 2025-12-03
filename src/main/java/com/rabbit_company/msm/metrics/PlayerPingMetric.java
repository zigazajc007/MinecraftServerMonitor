package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicReference;

public class PlayerPingMetric implements MetricProvider {
    private final Plugin plugin;
    private final int interval;
    private BukkitTask sampleTask;

    private final AtomicReference<Double> minPing = new AtomicReference<>(0.0);
    private final AtomicReference<Double> avgPing = new AtomicReference<>(0.0);
    private final AtomicReference<Double> maxPing = new AtomicReference<>(0.0);

    public PlayerPingMetric(Plugin plugin, int interval){
        this.plugin = plugin;
        this.interval = interval;
    }

    @Override
    public void start() {
        sampleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[0]);
            int count = players.length;

            if(count == 0){
                minPing.set(0.0);
                avgPing.set(0.0);
                maxPing.set(0.0);
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

                minPing.set(min);
                avgPing.set(sum / (double) count);
                maxPing.set(max);
            }
        }, 0L, 20L * this.interval);
    }

    @Override
    public void stop() {
        if (sampleTask != null) sampleTask.cancel();
    }

    @Override
    public String collect() {
        return "# HELP minecraft_min_player_ping_seconds Minimum player ping in seconds\n" +
                "# TYPE minecraft_min_player_ping_seconds gauge\n" +
                "# UNIT minecraft_min_player_ping_seconds seconds\n" +
                String.format("minecraft_min_player_ping_seconds %.3f\n", minPing.get() / 1000.0) +

                "# HELP minecraft_avg_player_ping_seconds Average player ping in seconds\n" +
                "# TYPE minecraft_avg_player_ping_seconds gauge\n" +
                "# UNIT minecraft_avg_player_ping_seconds seconds\n" +
                String.format("minecraft_avg_player_ping_seconds %.3f\n", avgPing.get() / 1000.0) +

                "# HELP minecraft_max_player_ping_seconds Maximum player ping in seconds\n" +
                "# TYPE minecraft_max_player_ping_seconds gauge\n" +
                "# UNIT minecraft_max_player_ping_seconds seconds\n" +
                String.format("minecraft_max_player_ping_seconds %.3f\n", maxPing.get() / 1000.0);
    }
}
