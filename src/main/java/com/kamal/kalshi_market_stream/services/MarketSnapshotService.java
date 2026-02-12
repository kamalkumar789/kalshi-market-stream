package com.kamal.kalshi_market_stream.services;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamal.kalshi_market_stream.DTOs.MarketSnapshotPointDTO;
import com.kamal.kalshi_market_stream.entities.Market;
import com.kamal.kalshi_market_stream.entities.MarketSnapshot;
import com.kamal.kalshi_market_stream.repositories.MarketSnapshotRepository;

@Service
public class MarketSnapshotService {

    private final MarketSnapshotRepository snapshotRepository;
    private static final Logger log = LoggerFactory.getLogger(MarketSnapshotService.class);

    public MarketSnapshotService(MarketSnapshotRepository snapshotRepository) {
        this.snapshotRepository = snapshotRepository;
    }

    @Transactional
    public MarketSnapshot storeSnapshot(
            Market market,
            Instant observedAt,
            Integer yesBid,
            Integer yesAsk,
            Integer noBid,
            Integer noAsk,
            Integer lastPrice,
            Integer volume24h,
            Integer openInterest) {

        log.debug("Storing snapshot: market={}, observedAt={}, lastPrice={}",
                market.getMarketTicker(), observedAt, lastPrice);

        MarketSnapshot snapshot = new MarketSnapshot();
        snapshot.setMarket(market);
        snapshot.setObservedAt(observedAt);
        snapshot.setYesBid(yesBid);
        snapshot.setYesAsk(yesAsk);
        snapshot.setNoBid(noBid);
        snapshot.setNoAsk(noAsk);
        snapshot.setLastPrice(lastPrice);

        MarketSnapshot saved = snapshotRepository.save(snapshot);

        log.debug("Snapshot saved: id={}, market={}",
                saved.getId(), market.getMarketTicker());

        return saved;
    }

    public List<MarketSnapshotPointDTO> fetch(
            String marketTicker,
            String status,
            Instant from,
            Instant to,
            int limit) {

        int safeLimit = Math.max(1, Math.min(limit, 5000));

        log.debug("Fetching snapshots: market={}, status={}, from={}, to={}, limit={}",
                marketTicker, status, from, to, safeLimit);

        Sort sort = Sort.by("DESC", "observedAt");
        Pageable pageable = PageRequest.of(0, safeLimit, sort);

        List<MarketSnapshot> rows;

        if (from != null && to != null) {
            rows = snapshotRepository
                    .findByMarket_MarketTickerAndMarket_StatusAndObservedAtBetween(
                            marketTicker, status, from, to, pageable);
        } else {
            rows = snapshotRepository
                    .findByMarket_MarketTickerAndMarket_Status(
                            marketTicker, status, pageable);
        }

        log.debug("Fetched {} snapshot rows for market={}", rows.size(), marketTicker);

        return rows.stream()
                .map(s -> new MarketSnapshotPointDTO(
                        s.getObservedAt(),
                        s.getYesBid(),
                        s.getNoBid(),
                        s.getLastPrice(),
                        s.getMarket().getSubtitle(),
                        s.getMarket().getStatus()))
                .collect(Collectors.toList());
    }
}
