package com.rabbit_company.msm.http;

import com.rabbit_company.msm.metrics.MetricRegistry;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.Plugin;

import java.net.InetSocketAddress;

public class MetricsHttpServer {
    private final Plugin plugin;
    private final MetricRegistry registry;
    private HttpServer server;

    public MetricsHttpServer(Plugin plugin, MetricRegistry registry){
        this.plugin = plugin;
        this.registry = registry;
    }

    public void start(){
        try {
            int port = plugin.getConfig().getInt("http.port", 9111);
            String token = plugin.getConfig().getString("http.token", "changeme");

            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/metrics", new MetricsHandler(token, registry));
            server.setExecutor(null);
            server.start();
            plugin.getLogger().info("Metrics server on port " + port);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to start metrics server " + e.getMessage());
        }
    }

    public void stop(){
        if(server != null) server.stop(0);
        server = null;
    }
}
