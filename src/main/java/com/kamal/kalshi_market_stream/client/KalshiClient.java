package com.kamal.kalshi_market_stream.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import com.kamal.kalshi_market_stream.dtos.KalshiEventResponseDTO;

@Component
public class KalshiClient {

    private final WebClient kaClient;

    public KalshiClient(
            WebClient.Builder builder,
            @Value("${kalshi.api.base-url}") String baseUrl) {

        this.kaClient = builder
                .baseUrl(baseUrl)
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
