package com.rabbit_company.msm;

import com.rabbit_company.msm.http.MetricsHttpServer;
import com.rabbit_company.msm.listeners.ChunkLoadListener;
import com.rabbit_company.msm.listeners.ChunkUnloadListener;
import com.rabbit_company.msm.listeners.EntityRemoveFromWorldListener;
import com.rabbit_company.msm.listeners.EntityAddToWorldListener;
import com.rabbit_company.msm.metrics.*;
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

        if(getConfig().getBoolean("metrics.memory.enabled", true))
            metricRegistry.register(new MemoryMetric(this, getConfig().getInt("metrics.memory.interval", 1)));

        if(getConfig().getBoolean("metrics.player_count.enabled", true))
            metricRegistry.register(new PlayerCountMetric(this, getConfig().getInt("metrics.player_count.interval", 1)));

        if(getConfig().getBoolean("metrics.player_ping.enabled", true))
            metricRegistry.register(new PlayerPingMetric(this, getConfig().getInt("metrics.player_ping.interval", 1)));

        if(getConfig().getBoolean("metrics.loaded_chunks.enabled", true))
            metricRegistry.register(new LoadedChunksMetric(this, getConfig().getInt("metrics.loaded_chunks.interval", 1), getConfig().getString("metrics.loaded_chunks.counting_method", "sampling")));

        if(getConfig().getBoolean("metrics.loaded_entities.enabled", true))
            metricRegistry.register(new LoadedEntitiesMetric(this, getConfig().getInt("metrics.loaded_entities.interval", 1), getConfig().getString("metrics.loaded_entities.counting_method", "sampling"), getConfig().getBoolean("metrics.loaded_entities.count_types", false)));

        if ((getConfig().getBoolean("metrics.loaded_chunks.enabled", true) && getConfig().getString("metrics.loaded_chunks.counting_method", "sampling").equalsIgnoreCase("event"))) {
            new ChunkLoadListener(this);
            new ChunkUnloadListener(this);
        }

        if(getConfig().getBoolean("metrics.loaded_entities.enabled", true) && getConfig().getString("metrics.loaded_entities.counting_method", "sampling").equalsIgnoreCase("event")){
            new EntityAddToWorldListener(this);
            new EntityRemoveFromWorldListener(this);
        }

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
