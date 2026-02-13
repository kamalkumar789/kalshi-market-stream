package com.kamal.kalshi_market_stream.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Component
public class SeriesZoneResolver {

    private final ZoneId defaultZone;
    private final Map<String, ZoneId> seriesZones = new HashMap<>();

    public SeriesZoneResolver(
            @Value("${kalshi.market.defaultZone:America/New_York}") String defaultZoneId,
            @Value("#{${kalshi.market.seriesZones:{}}}") Map<String, String> raw
    ) {
        this.defaultZone = ZoneId.of(defaultZoneId);
        if (raw != null) {
            raw.forEach((k, v) -> seriesZones.put(k, ZoneId.of(v)));
        }
    }

    public ZoneId zoneForSeries(String seriesTicker) {
        return seriesZones.getOrDefault(seriesTicker, defaultZone);
    }
}
