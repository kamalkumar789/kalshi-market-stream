package com.kamal.kalshi_market_stream.controllers;

import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.kamal.kalshi_market_stream.DTOs.MarketSnapshotPointDTO;
import com.kamal.kalshi_market_stream.services.MarketSnapshotService;

@RestController
@RequestMapping("/api/markets")
public class MarketSnapshotRangeController {

    private final MarketSnapshotService rangeService;
    private static final Logger log = LoggerFactory.getLogger(MarketSnapshotRangeController.class);


    public MarketSnapshotRangeController(MarketSnapshotService rangeService) {
        this.rangeService = rangeService;
    }

    @GetMapping("/{marketTicker}/snapshots")
    public List<MarketSnapshotPointDTO> getSnapshots(
            @PathVariable String marketTicker,
            @RequestParam("status") String status, // ACTIVE or SETTLED
            @RequestParam(value = "from", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(value = "to", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(value = "limit", required = false, defaultValue = "100") int limit) {
        return rangeService.fetch(marketTicker, status, from, to, limit);
    }
}
