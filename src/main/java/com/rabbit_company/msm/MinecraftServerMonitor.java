package com.rabbit_company.msm;

import com.rabbit_company.msm.http.MetricsHttpServer;
import com.rabbit_company.msm.metrics.MetricRegistry;
import com.rabbit_company.msm.metrics.PlayerCountMetric;
import com.rabbit_company.msm.metrics.PlayerPingMetric;
import com.rabbit_company.msm.metrics.TpsMetric;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecraftServerMonitor extends JavaPlugin {
    private MetricsHttpServer httpServer;
    private MetricRegistry metricRegistry;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        metricRegistry = new MetricRegistry();

        if(getConfig().getBoolean("metrics.tps.enabled", true))
            metricRegistry.register(new TpsMetric(this, getConfig().getInt("metrics.tps.interval", 1)));

        if(getConfig().getBoolean("metrics.player_count.enabled", true))
            metricRegistry.register(new PlayerCountMetric(this, getConfig().getInt("metrics.player_count.interval", 1)));

        if(getConfig().getBoolean("metrics.player_ping.enabled", true))
            metricRegistry.register(new PlayerPingMetric(this, getConfig().getInt("metrics.player_ping.interval", 1)));

        httpServer = new MetricsHttpServer(
                this,
                getConfig().getInt("http.port", 9111),
                getConfig().getString("http.token", "changeme"),
                metricRegistry
        );

        httpServer.start();
        getLogger().info("MinecraftServerMonitor started");
    }

    @Override
    public void onDisable() {
        if (httpServer != null) httpServer.stop();
        if (metricRegistry != null) metricRegistry.shutdown();
    }
}
