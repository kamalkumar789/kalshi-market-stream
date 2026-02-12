package com.kamal.kalshi_market_stream.DTOs;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class KalshiEventResponseDTO {

    private EventDTO event;
    private List<MarketDTO> markets;

    @Data
    public static class EventDTO {
        private String event_ticker;
        private String series_ticker;
        private String title;
        private String sub_title;
        private String category;
        private Instant strike_date;
        private List<MarketDTO> markets;
    }

    @Data
    public static class MarketDTO {
        private String ticker;
        private String event_ticker;

        private String title;
        private String subtitle;

        private Instant created_time;
        private Instant updated_time;
        private Instant open_time;
        private Instant close_time;
        private Instant expiration_time;

        private String status;
        private String response_price_units;

        private Integer yes_bid;
        private Integer yes_ask;
        private Integer no_bid;
        private Integer no_ask;

        private Integer last_price;
        private Integer volume_24h;
        private Integer open_interest;

        private String result;
    }
}
