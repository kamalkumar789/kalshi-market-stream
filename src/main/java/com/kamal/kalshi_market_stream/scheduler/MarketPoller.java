package com.kamal.kalshi_market_stream.scheduler;

import java.time.Instant;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kamal.kalshi_market_stream.DTOs.KalshiMarketsResponseDTO;
import com.kamal.kalshi_market_stream.DTOs.KalshiMarketsResponseDTO.MarketDTO;
import com.kamal.kalshi_market_stream.client.KalshiClient;
import com.kamal.kalshi_market_stream.entities.Market;
import com.kamal.kalshi_market_stream.services.MarketService;
import com.kamal.kalshi_market_stream.services.MarketSnapshotService;

@Service
public class MarketPoller {

    private final KalshiClient client;
    private final MarketService marketService;
    private final MarketSnapshotService snapshotService;

    public MarketPoller(KalshiClient client, MarketService marketService, MarketSnapshotService snapshotService) {
        this.client = client;
        this.marketService = marketService;
        this.snapshotService = snapshotService;
    }

    @Scheduled(fixedDelay = 10000)
    public void poll() {
        KalshiMarketsResponseDTO data = client.getSeries("KXHIGHNY", 3);
        Instant observedAt = Instant.now();

        for (MarketDTO dto : data.getMarkets()) {
            
            // 1) Upsert Market
            Market market = marketService.storeOrUpdateMarket(
                    dto.getTicker(),
                    dto.getEventTicker(),
                    dto.getTitle(),
                    dto.getSubtitle(),
                    dto.getResponsePriceUnits(),
                    parseInstant(dto.getOpenTime()),
                    parseInstant(dto.getCloseTime()),
                    parseInstant(dto.getExpirationTime()),
                    dto.getStatus(),
                    parseInstant(dto.getCreatedTime()),
                    parseInstant(dto.getUpdatedTime()));
                
            if (market.getResult() != null && !market.getResult().isEmpty()) {
                System.out.println("Market settled. Skipping snapshots: " + market.getMarketTicker());
                continue;
            }

            // 2) Store snapshot ALWAYS (even if unchanged)

            System.out.println("market_snapshot" + dto.getYesAsk());
            snapshotService.storeSnapshot(
                    market,
                    observedAt,
                    dto.getYesBid(),
                    dto.getYesAsk(),
                    dto.getNoBid(),
                    dto.getNoAsk(),
                    dto.getLastPrice(),
                    dto.getVolume24h(),
                    dto.getOpenInterest());
        }
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isEmpty())
            return null;
        return Instant.parse(value);
    }

}
