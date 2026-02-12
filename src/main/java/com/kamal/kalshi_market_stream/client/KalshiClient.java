package com.kamal.kalshi_market_stream.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.kamal.kalshi_market_stream.DTOs.KalshiMarketsResponseDTO;

@Component
public class KalshiClient {
    

    private final WebClient kaClient;

    public KalshiClient(WebClient.Builder builder) {
        this.kaClient = builder
                .baseUrl("https://api.elections.kalshi.com/trade-api/v2")
                .build();
    }

    public KalshiMarketsResponseDTO getMarketDataByEvent(String event_ticker) {
        return kaClient.get()
                .uri("/markets?event_ticker={event_ticker}", event_ticker)
                .retrieve()
                .bodyToMono(KalshiMarketsResponseDTO.class)
                .block(); 
    }
}
