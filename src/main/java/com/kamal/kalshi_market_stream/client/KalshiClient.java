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

    public KalshiMarketsResponseDTO getSeries(String ticker, int limit) {
        return kaClient.get()
                .uri("/markets?limit={limit}&series_ticker={ticket}", limit, ticker)
                .retrieve()
                .bodyToMono(KalshiMarketsResponseDTO.class)
                .block(); 
    }
}
