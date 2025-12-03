package com.rabbit_company.msm.http;

import com.rabbit_company.msm.metrics.MetricRegistry;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class MetricsHandler implements HttpHandler {
    private final String bearer;
    private final MetricRegistry registry;

    public MetricsHandler(String bearer, MetricRegistry registry){
        this.bearer = bearer;
        this.registry = registry;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if(!"GET".equalsIgnoreCase(exchange.getRequestMethod())){
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        String auth = exchange.getRequestHeaders().getFirst("Authorization");
        if(auth == null || !auth.equals(bearer)) {
            exchange.sendResponseHeaders(401, -1);
            return;
        }

        String out = registry.collectAll();
        byte[] bytes = out.getBytes(StandardCharsets.UTF_8);

        exchange.getResponseHeaders().add("Content-Type", "application/openmetrics-text; version=1.0.0; charset=utf-8");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }
}
