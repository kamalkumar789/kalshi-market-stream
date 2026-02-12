package com.kamal.kalshi_market_stream.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

import com.kamal.kalshi_market_stream.utils.SignalsEngine;

@Configuration
public class Config {

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public SignalsEngine signalEngine(){
        return new SignalsEngine();
    }

    
}
