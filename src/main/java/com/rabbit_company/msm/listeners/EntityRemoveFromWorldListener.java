package com.rabbit_company.msm.listeners;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import com.rabbit_company.msm.metrics.LoadedEntitiesMetric;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class EntityRemoveFromWorldListener implements Listener {

    private final Plugin plugin;

    public EntityRemoveFromWorldListener(Plugin plugin){
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityRemoveFromWorld(final EntityRemoveFromWorldEvent e) {
        LoadedEntitiesMetric.entitiesCounts.computeIfPresent(e.getEntity().getWorld().getName(), (key, value) -> Math.max(0, value - 1));
    }
}
