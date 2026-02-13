package com.kamal.kalshi_market_stream.DTOs;

public record MarketListItemDTO(
        String marketTicker,
        String title,
        String subtitle,
        String status
) {}