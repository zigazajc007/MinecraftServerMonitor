package com.rabbit_company.msm;

import com.rabbit_company.msm.commands.Reload;
import com.rabbit_company.msm.http.MetricsHttpServer;
import com.rabbit_company.msm.listeners.ChunkLoadListener;
import com.rabbit_company.msm.listeners.ChunkUnloadListener;
import com.rabbit_company.msm.listeners.EntityRemoveFromWorldListener;
import com.rabbit_company.msm.listeners.EntityAddToWorldListener;
import com.rabbit_company.msm.metrics.*;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecraftServerMonitor extends JavaPlugin {

    private static MinecraftServerMonitor instance;
    public static MetricsHttpServer httpServer;
    public final static MetricRegistry metricRegistry = new MetricRegistry();

    public static MinecraftServerMonitor getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        if(!isPaperLike()){
            getLogger().severe("Plugin is only supported on Paper, PurPur, PufferFish and other Paper forks!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        if(!isMinecraftVersionAtLeast(1, 20, 0)){
            getLogger().severe("Plugin only supports Minecraft server versions 1.20.0 or higher!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info(Bukkit.getServer().getName());

        saveDefaultConfig();

        metricRegistry.register(new TpsMetric(this));
        metricRegistry.register(new MemoryMetric(this));
        metricRegistry.register(new PlayerCountMetric(this));
        metricRegistry.register(new PlayerPingMetric(this));
        metricRegistry.register(new LoadedChunksMetric(this));
        metricRegistry.register(new LoadedEntitiesMetric(this));

        new ChunkLoadListener(this);
        new ChunkUnloadListener(this);

        if(isMinecraftVersionAtLeast(1, 21, 0)) {
            new EntityAddToWorldListener(this);
            new EntityRemoveFromWorldListener(this);
        }else{
            getLogger().warning("Minecraft server version 1.21.0 or higher is required to use 'event' counting method for loaded entities!");
        }

        if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            getLogger().info("PlaceholderAPI plugin detected. Enabling custom metrics...");
            metricRegistry.register(new PlaceholdersMetric(this));
        } else {
            getLogger().info("PlaceholderAPI plugin not detected. Custom metrics have been disabled.");
        }

        this.getCommand("reload").setExecutor(new Reload());

        httpServer = new MetricsHttpServer(this, metricRegistry);
        httpServer.start();

        getLogger().info("MinecraftServerMonitor started");
    }

    @Override
    public void onDisable() {
        if (httpServer != null) httpServer.stop();
        metricRegistry.stop();
    }

    public boolean isMinecraftVersionAtLeast(int reqMajor, int reqMinor, int reqPatch) {
        String ver = Bukkit.getMinecraftVersion();

        String[] parts = ver.split("\\.");
        int major = Integer.parseInt(parts[0]);
        int minor = Integer.parseInt(parts[1]);
        int patch = parts.length > 2 ? Integer.parseInt(parts[2]) : 0;

        if (major != reqMajor) {
            return major > reqMajor;
        }

        if (minor != reqMinor) {
            return minor > reqMinor;
        }

        return patch >= reqPatch;
    }

    private boolean isPaperLike() {
        try {
            Bukkit.class.getMethod("getTPS");
            World.class.getMethod("getPlayerCount");
            World.class.getMethod("getEntityCount");
            World.class.getMethod("getChunkCount");
            return true;
        } catch (NoSuchMethodException ignored) {}

        return false;
    }
}
