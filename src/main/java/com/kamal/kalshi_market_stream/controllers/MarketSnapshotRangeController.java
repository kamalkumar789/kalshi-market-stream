package com.kamal.kalshi_market_stream.controllers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.kamal.kalshi_market_stream.dtos.MarketSnapshotPointDTO;
import com.kamal.kalshi_market_stream.services.MarketSnapshotService;

@RestController
@RequestMapping("/api/events")
@CrossOrigin("*")
public class MarketSnapshotRangeController {

    private final MarketSnapshotService rangeService;

    public MarketSnapshotRangeController(MarketSnapshotService rangeService) {
        this.rangeService = rangeService;
    }

    private static final Logger log = LoggerFactory.getLogger(MarketSnapshotRangeController.class);

    @GetMapping("/{eventTicker}/markets/{marketTicker}/snapshots")
    public List<MarketSnapshotPointDTO> getSnapshots(
            @PathVariable String eventTicker,
            @PathVariable String marketTicker,
            @RequestParam(defaultValue = "active") String status,
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime from,
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") LocalDateTime to,
            @RequestParam(value = "limit", defaultValue = "20") int limit) {

        log.info("snapshots request -> eventTicker={}, marketTicker={}, status={}, from={}, to={}, limit={}",
                eventTicker, marketTicker, status, from, to, limit);

        return rangeService.fetchByEventAndMarket(eventTicker, marketTicker, status, from, to, limit);
    }
}
