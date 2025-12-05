package com.rabbit_company.msm.listeners;

import com.rabbit_company.msm.metrics.LoadedChunksMetric;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.plugin.Plugin;

public class ChunkUnloadListener implements Listener {

    private final Plugin plugin;

    public ChunkUnloadListener(Plugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(final ChunkUnloadEvent e) {
        LoadedChunksMetric.worldChunkCounts.computeIfPresent(e.getWorld().getName(), (key, value) -> Math.max(0, value - 1));
    }
}
