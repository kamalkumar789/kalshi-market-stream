package com.kamal.kalshi_market_stream.scheduler;

import java.time.Instant;

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

    private final KalshiClient client;
    private final MarketService marketService;
    private final MarketSnapshotService snapshotService;
    private final MarketLatencyService latencyService;
    private final SignalsEngine signalsEngine;

    public MarketPoller(KalshiClient client, MarketService marketService, MarketSnapshotService snapshotService,
            MarketLatencyService latencyService, SignalsEngine signalsEngine) {
        this.client = client;
        this.marketService = marketService;
        this.snapshotService = snapshotService;
        this.latencyService = latencyService;
        this.signalsEngine = signalsEngine;
    }

    @Scheduled(fixedDelay = 2)
    public void poll() {

        KalshiMarketsResponseDTO data = client.getSeries("KXHIGHNY", 1);

        // when api responded (y).

        Instant receivedTs = Instant.now();

        for (MarketDTO dto : data.getMarkets()) {

            // when event happened is the official date coming from api (x).
            Instant exchangeTs = parseInstant(dto.getUpdatedTime());

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
            MarketSnapshot snapshot = snapshotService.storeSnapshot(
                    market,
                    exchangeTs,
                    dto.getYesBid(),
                    dto.getYesAsk(),
                    dto.getNoBid(),
                    dto.getNoAsk(),
                    dto.getLastPrice(),
                    dto.getVolume24h(),
                    dto.getOpenInterest());

            // processed means after storing into db as per my need means process time (z)
            Instant processedTs = Instant.now();

            latencyService.recordLatency(
                    snapshot,
                    exchangeTs,
                    receivedTs,
                    processedTs,
                    "REST");

            Signals.Trend signal = signalsEngine.update(dto.getEventTicker(), dto.getYesAsk());

            System.out.printf(
                    "Event=%s Market=%s Price=%d → Signal=%s%n",
                    dto.getEventTicker(),
                    dto.getTicker(),
                    dto.getYesAsk(),
                    signal);

        }
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isEmpty())
            return null;
        return Instant.parse(value);
    }

}
