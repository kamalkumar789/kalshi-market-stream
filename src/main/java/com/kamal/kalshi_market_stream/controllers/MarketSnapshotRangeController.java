package com.kamal.kalshi_market_stream.controllers;

import java.time.Instant;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.kamal.kalshi_market_stream.DTOs.MarketSnapshotPointDTO;
import com.kamal.kalshi_market_stream.services.MarketSnapshotService;

@RestController
@RequestMapping("/api/markets")
public class MarketSnapshotRangeController {

    private final MarketSnapshotService rangeService;

    public MarketSnapshotRangeController(MarketSnapshotService rangeService) {
        this.rangeService = rangeService;
    }

    @GetMapping("/{marketTicker}/snapshots")
    public List<MarketSnapshotPointDTO> getSnapshots(
            @PathVariable String marketTicker,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        System.out.println(marketTicker);
        return rangeService.fetch(marketTicker, from, to);
    }
}
