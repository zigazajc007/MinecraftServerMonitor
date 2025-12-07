package com.rabbit_company.msm.metrics;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class TpsMetric implements MetricProvider {
    private final Plugin plugin;
    private boolean enabled = false;

    private volatile double tps1m = 20.0;
    private volatile double tps5m = 20.0;
    private volatile double tps15m = 20.0;

    private BukkitTask calculateTask;

    public TpsMetric(Plugin plugin){
        this.plugin = plugin;
    }

    @Override
    public void start() {
        if (!plugin.getConfig().getBoolean("metrics.tps.enabled", true)) return;

        int interval = plugin.getConfig().getInt("metrics.tps.interval", 1);
        calculateTask = Bukkit.getScheduler().runTaskTimer(plugin, this::updateTPSFromPaper, 100L, 20L * interval);
        enabled = true;
    }

    private void updateTPSFromPaper() {
        double[] paperTps = Bukkit.getTPS();

        if (paperTps.length >= 1) {
            tps1m = Math.min(20.0, Math.max(0.0, paperTps[0]));
        }
        if (paperTps.length >= 2) {
            tps5m = Math.min(20.0, Math.max(0.0, paperTps[1]));
        }
        if (paperTps.length >= 3) {
            tps15m = Math.min(20.0, Math.max(0.0, paperTps[2]));
        }
    }

    @Override
    public void stop() {
        enabled = false;
        if (calculateTask != null) calculateTask.cancel();
        calculateTask = null;
    }

    @Override
    public String collect() {

        return "# HELP minecraft_tps Server ticks per second\n" +
                "# TYPE minecraft_tps gauge\n" +
                String.format("minecraft_tps{interval=\"1m\"} %.2f\n", tps1m) +
                String.format("minecraft_tps{interval=\"5m\"} %.2f\n", tps5m) +
                String.format("minecraft_tps{interval=\"15m\"} %.2f\n", tps15m);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
