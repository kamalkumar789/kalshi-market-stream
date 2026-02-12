package com.kamal.kalshi_market_stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KalshiMarketStreamApplication {

	public static void main(String[] args) {
		SpringApplication.run(KalshiMarketStreamApplication.class, args);
	}

}
