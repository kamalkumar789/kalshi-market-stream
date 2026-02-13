package com.kamal.kalshi_market_stream.controllers;

import java.util.List;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.kamal.kalshi_market_stream.DTOs.MarketListItemDTO;
import com.kamal.kalshi_market_stream.services.MarketService;

@RestController
@RequestMapping("/api/events")
@CrossOrigin("*")
public class MarketController {

    private final MarketService marketService;

    public MarketController(MarketService marketService) {
        this.marketService = marketService;
    }

    @GetMapping("/{eventTicker}/markets")
    public List<MarketListItemDTO> getMarketsByEvent(
            @PathVariable String eventTicker,
            @RequestParam(value = "status", required = false) String status // optional
    ) {
        return marketService.listMarketsByEvent(eventTicker, status);
    }
}
