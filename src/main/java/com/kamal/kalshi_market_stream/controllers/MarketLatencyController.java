package com.kamal.kalshi_market_stream.controllers;

import com.kamal.kalshi_market_stream.DTOs.MarketLatencyResponseDTO;
import com.kamal.kalshi_market_stream.services.MarketLatencyService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/markets")
public class MarketLatencyController {

    private static final Logger log =
            LoggerFactory.getLogger(MarketLatencyController.class);

    @Autowired
    private final MarketLatencyService service;

    public MarketLatencyController(MarketLatencyService service) {
        this.service = service;
    }

    @GetMapping("/{marketTicker}/latencies")
    public List<MarketLatencyResponseDTO> getLatencies(
            @PathVariable String marketTicker,
            @RequestParam(defaultValue = "50") int limit
    ) {
        log.info("Fetching latencies | market={} limit={}", marketTicker, limit);

        List<MarketLatencyResponseDTO> result =
                service.getRecentFreshLatencies(marketTicker, limit);

        log.info("Returned {} latency records for market={}",
                result.size(), marketTicker);

        return result;
    }
}
