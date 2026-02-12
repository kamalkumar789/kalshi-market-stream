package com.kamal.kalshi_market_stream.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        MarketSnapshot snapshot = new MarketSnapshot();
        snapshot.setMarket(market);
        snapshot.setObservedAt(observedAt);
        snapshot.setYesBid(yesBid);
        snapshot.setYesAsk(yesAsk);
        snapshot.setNoBid(noBid);
        snapshot.setNoAsk(noAsk);
        snapshot.setLastPrice(lastPrice);
        snapshot.setVolume24h(volume24h);
        snapshot.setOpenInterest(openInterest);

        return snapshotRepository.save(snapshot);
    }

    public List<MarketSnapshotPointDTO> fetch(
            String marketTicker,
            String status,
            Instant from,
            Instant to,
            int limit) {
        // safety caps so someone doesn't request 1M rows
        int safeLimit = Math.max(1, Math.min(limit, 5000));

        Sort sort = Sort.by("DESC", "observedAt");
        Pageable pageable = PageRequest.of(0, safeLimit, sort);

        List<MarketSnapshot> rows;

        if (from != null && to != null) {
            rows = snapshotRepository.findByMarket_MarketTickerAndMarket_StatusAndObservedAtBetween(
                    marketTicker, status, from, to, pageable);
        } else {
            rows = snapshotRepository.findByMarket_MarketTickerAndMarket_Status(
                    marketTicker, status, pageable);
        }

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
