package com.kamal.kalshi_market_stream.scheduler;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kamal.kalshi_market_stream.DTOs.KalshiMarketsResponseDTO;
import com.kamal.kalshi_market_stream.DTOs.KalshiMarketsResponseDTO.MarketDTO;
import com.kamal.kalshi_market_stream.client.KalshiClient;
import com.kamal.kalshi_market_stream.entities.Market;
import com.kamal.kalshi_market_stream.entities.MarketSnapshot;
import com.kamal.kalshi_market_stream.services.MarketService;
import com.kamal.kalshi_market_stream.services.MarketLatencyService;
import com.kamal.kalshi_market_stream.services.MarketSnapshotService;
import com.kamal.kalshi_market_stream.utils.Signals;
import com.kamal.kalshi_market_stream.utils.SignalsEngine;

@Service
public class MarketPoller {

    private static final Logger log = LoggerFactory.getLogger(MarketPoller.class);

    private final KalshiClient client;
    private final MarketService marketService;
    private final MarketSnapshotService snapshotService;
    private final MarketLatencyService latencyService;
    private final SignalsEngine signalsEngine;

    public MarketPoller(
            KalshiClient client,
            MarketService marketService,
            MarketSnapshotService snapshotService,
            MarketLatencyService latencyService,
            SignalsEngine signalsEngine
    ) {
        this.client = client;
        this.marketService = marketService;
        this.snapshotService = snapshotService;
        this.latencyService = latencyService;
        this.signalsEngine = signalsEngine;
    }

    @Scheduled(fixedDelay = 2000)
    public void poll() {
        KalshiMarketsResponseDTO data = client.getSeries("KXHIGHNY", 1);

        // y: when API response is received by your server
        Instant receivedTs = Instant.now();

        for (MarketDTO dto : data.getMarkets()) {

            // x: exchange last update time from payload
            Instant exchangeTs = parseInstant(dto.getUpdatedTime());

            Market market = marketService.storeOrUpdateMarket(
                    dto.getTicker(),
                    dto.getEventTicker(),
                    dto.getTitle(),
                    dto.getSubtitle(),
                    parseInstant(dto.getOpenTime()),
                    parseInstant(dto.getCloseTime()),
                    dto.getStatus(),
                    parseInstant(dto.getCreatedTime()),
                    exchangeTs
            );

            if (market.getResult() != null && !market.getResult().isEmpty()) {
                // market settled, no need to keep generating signals
                signalsEngine.remove(dto.getEventTicker());
                log.debug("Market settled. Skipping snapshots | market={}", market.getMarketTicker());
                continue;
            }

            MarketSnapshot snapshot = snapshotService.storeSnapshot(
                    market,
                    exchangeTs,
                    dto.getYesBid(),
                    dto.getYesAsk(),
                    dto.getNoBid(),
                    dto.getNoAsk(),
                    dto.getLastPrice(),
                    dto.getVolume24h(),
                    dto.getOpenInterest()
            );

            // z: after DB write
            Instant processedTs = Instant.now();

            latencyService.recordLatency(
                    snapshot,
                    exchangeTs,
                    receivedTs,
                    processedTs,
                    "REST"
            );

            // Prefer mid price to reduce spread noise
            int signalPrice = computeYesMid(dto);

            Signals.Trend trend = signalsEngine.update(dto.getEventTicker(), signalPrice);

            // Keep logs light: only one line per market per poll (change to debug if too noisy)
            log.info("Signal | event={} market={} price={} trend={}",
                    dto.getEventTicker(), dto.getTicker(), signalPrice, trend);
        }
    }

    private int computeYesMid(MarketDTO dto) {
        Integer bid = dto.getYesBid();
        Integer ask = dto.getYesAsk();

        if (bid != null && ask != null) {
            return (bid + ask) / 2;
        }
        if (ask != null) return ask;
        if (bid != null) return bid;
        return 0; 
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isEmpty()) return null;
        return Instant.parse(value);
    }
}
