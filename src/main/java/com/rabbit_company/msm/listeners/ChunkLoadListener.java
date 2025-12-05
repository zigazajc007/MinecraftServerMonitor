package com.rabbit_company.msm.listeners;

import com.rabbit_company.msm.metrics.LoadedChunksMetric;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;

public class ChunkLoadListener implements Listener {

    private final Plugin plugin;

    public ChunkLoadListener(Plugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkLoad(final ChunkLoadEvent e) {
        LoadedChunksMetric.worldChunkCounts.compute(e.getWorld().getName(), (key, value) -> (value == null) ? 1 : value + 1);
    }
}
