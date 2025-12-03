package com.rabbit_company.msm.metrics;

import java.util.ArrayList;
import java.util.List;

public class MetricRegistry {
    private final List<MetricProvider> providers = new ArrayList<>();

    public void register(MetricProvider provider){
        providers.add(provider);
        provider.start();
    }

    public String collectAll(){
        StringBuilder sb = new StringBuilder();

        for (MetricProvider p : providers) sb.append(p.collect());
        sb.append("# EOF\n");

        return sb.toString();
    }

    public void shutdown() {
        for (MetricProvider p : providers) p.stop();
    }
}
