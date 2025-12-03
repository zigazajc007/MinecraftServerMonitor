package com.rabbit_company.msm.http;

import com.rabbit_company.msm.metrics.MetricRegistry;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.plugin.Plugin;

import java.net.InetSocketAddress;

public class MetricsHttpServer {
    private final Plugin plugin;
    private final int port;
    private final String token;
    private final MetricRegistry registry;
    private HttpServer server;

    public MetricsHttpServer(Plugin plugin, int port, String token, MetricRegistry registry){
        this.plugin = plugin;
        this.port = port;
        this.token = "Bearer " + token;
        this.registry = registry;
    }

    public void start(){
        try {
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
    }
}
