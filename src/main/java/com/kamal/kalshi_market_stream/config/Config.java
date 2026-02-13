package com.kamal.kalshi_market_stream.config;

import java.time.ZoneId;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.kamal.kalshi_market_stream.utils.SignalsEngine;

@Configuration
public class Config {

    @Value("${app.timezone}")
    private String zone;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public SignalsEngine signalEngine(){
        return new SignalsEngine();
    }

    @Bean
    public Executor marketPollExecutor() {
        return Executors.newFixedThreadPool(2);
    }

    @Bean
    public ZoneId appZoneId() {
        return ZoneId.of(zone);
    }
    
}
