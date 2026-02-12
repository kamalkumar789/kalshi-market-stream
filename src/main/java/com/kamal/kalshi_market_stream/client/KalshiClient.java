package com.kamal.kalshi_market_stream.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.kamal.kalshi_market_stream.DTOs.KalshiEventResponseDTO;

@Component
public class KalshiClient {

    private final WebClient kaClient;

    public KalshiClient(WebClient.Builder builder) {
        this.kaClient = builder
                .baseUrl("https://api.elections.kalshi.com/trade-api/v2")
                .build();
    }

    public KalshiEventResponseDTO getEvent(String eventTicker) {
        return kaClient.get()
                .uri("/events/{eventTicker}", eventTicker)
                .retrieve()
                .bodyToMono(KalshiEventResponseDTO.class)
                .block();
    }
}
