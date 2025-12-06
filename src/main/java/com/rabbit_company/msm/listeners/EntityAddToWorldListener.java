package com.rabbit_company.msm.listeners;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.rabbit_company.msm.metrics.LoadedEntitiesMetric;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

public class EntityAddToWorldListener implements Listener {

    private final Plugin plugin;

    public EntityAddToWorldListener(Plugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityAddToWorld(final EntityAddToWorldEvent e) {
        LoadedEntitiesMetric.entitiesCounts.compute(e.getWorld().getName(), (key, value) -> (value == null) ? 1 : value + 1);
    }
}
