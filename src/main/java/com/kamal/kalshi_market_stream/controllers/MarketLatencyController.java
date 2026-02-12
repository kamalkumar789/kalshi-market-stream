package com.kamal.kalshi_market_stream.controllers;

import com.kamal.kalshi_market_stream.DTOs.MarketLatencyResponseDTO;
import com.kamal.kalshi_market_stream.services.MarketLatencyService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/markets")
public class MarketLatencyController {

    private final MarketLatencyService service;

    public MarketLatencyController(MarketLatencyService service) {
        this.service = service;
    }

    @GetMapping("/{marketTicker}/latencies")
    public List<MarketLatencyResponseDTO> getLatencies(
            @PathVariable String marketTicker,
            @RequestParam(defaultValue = "50") int limit
    ) {
        return service.getRecentFreshLatencies(marketTicker, limit);
    }
}
