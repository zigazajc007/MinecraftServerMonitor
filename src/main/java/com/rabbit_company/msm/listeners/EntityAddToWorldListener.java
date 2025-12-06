package com.rabbit_company.msm.listeners;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import com.rabbit_company.msm.metrics.LoadedEntitiesMetric;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.ConcurrentHashMap;

public class EntityAddToWorldListener implements Listener {

    private final Plugin plugin;

    public EntityAddToWorldListener(Plugin plugin) {
        this.plugin = plugin;

        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityAddToWorld(final EntityAddToWorldEvent e) {
        String world = e.getWorld().getName();
        String entity = e.getEntity().getType().name();

        LoadedEntitiesMetric.entitiesCounts.compute(world, (key, value) -> (value == null) ? 1 : value + 1);
        LoadedEntitiesMetric.entityTypesCounts.computeIfAbsent(world, w -> new ConcurrentHashMap<>()).compute(entity, (key, value) -> (value == null) ? 1 : value + 1);
    }
}
