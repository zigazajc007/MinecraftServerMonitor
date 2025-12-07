package com.rabbit_company.msm.metrics;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PlaceholdersMetric implements MetricProvider {
    private final Plugin plugin;

    private final List<Integer> taskIds = new ArrayList<>();
    private final ConcurrentHashMap<String, CustomMetric> customMetrics = new ConcurrentHashMap<>();

    public PlaceholdersMetric(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void start() {
        ConfigurationSection metrics = plugin.getConfig().getConfigurationSection("metrics.custom");

        if(metrics == null) {
            plugin.getLogger().info("No custom placeholder metrics detected");
            return;
        }

        for (String id : metrics.getKeys(false)) {
            ConfigurationSection metric = metrics.getConfigurationSection(id);
            if (metric == null) continue;

            String description = metric.getString("description", "");
            String type = metric.getString("type", "gauge");
            String placeholder = metric.getString("placeholder", "");
            int interval = metric.getInt("interval", 1);
            boolean perPlayer = metric.getBoolean("per_player", false);

            CustomMetric cm = new CustomMetric(id, description, type, placeholder, interval, perPlayer);
            customMetrics.put(id, cm);
        }

        for (Map.Entry<String, CustomMetric> entry : customMetrics.entrySet()) {
            CustomMetric metric = entry.getValue();

            BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                if(!metric.isPerPlayer()) {
                    metric.setGlobalValue(PlaceholderAPI.setPlaceholders(null, metric.getPlaceholder()));
                    return;
                }

                // Per player metrics
                for (Player player : Bukkit.getOnlinePlayers()) {
                    metric.setPlayerValue(player.getName(), PlaceholderAPI.setPlaceholders(player, metric.getPlaceholder()));
                }
            }, 0L, 20L * metric.getInterval());

            taskIds.add(task.getTaskId());
        }

        plugin.getLogger().info("Loaded " + customMetrics.size() + " custom placeholder metrics");
    }

    @Override
    public void stop() {
        for (int id : taskIds) {
            Bukkit.getScheduler().cancelTask(id);
        }
        taskIds.clear();
        customMetrics.clear();
    }

    @Override
    public String collect() {
        StringBuilder sb = new StringBuilder();

        for (CustomMetric metric : customMetrics.values()) {
            sb.append("# HELP ").append(metric.getName()).append(" ").append(metric.getDescription()).append("\n");
            sb.append("# TYPE ").append(metric.getName()).append(" ").append(metric.getType()).append("\n");

            if (!metric.isPerPlayer()) {
                MetricValue gv = metric.getGlobalValue();
                if (gv != null && !gv.value().contains("%")) {
                    sb.append(String.format(
                            "%s %s %.3f\n",
                            metric.getName(),
                            gv.value(),
                            gv.timestamp() / 1000.0
                    ));
                }
                continue;
            }

            for (Map.Entry<String, MetricValue> entry : metric.getPlayerValues().entrySet()) {
                String player = entry.getKey();
                MetricValue mv = entry.getValue();

                if (mv.value().contains("%")) continue;

                sb.append(String.format(
                        "%s{player=\"%s\"} %s %.3f\n",
                        metric.getName(),
                        player,
                        mv.value(),
                        mv.timestamp() / 1000.0
                ));
            }
        }

        return sb.toString();
    }
}
