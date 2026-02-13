package com.kamal.kalshi_market_stream.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kamal.kalshi_market_stream.entities.MarketSnapshot;

@Repository
public interface MarketSnapshotRepository extends JpaRepository<MarketSnapshot, Long> {

    List<MarketSnapshot> findByMarketId(Long marketId, Pageable pageable);

    List<MarketSnapshot> findByMarketIdAndObservedAtBetween(
            Long marketId,
            Instant from,
            Instant to,
            Pageable pageable
    );

    List<MarketSnapshot> findByMarket_MarketTickerAndMarket_StatusAndObservedAtBetween(
            String marketTicker,
            String status,
            Instant from,
            Instant to,
            Pageable pageable
    );

    List<MarketSnapshot> findByMarket_MarketTickerOrderByObservedAtDesc(
            String marketTicker,
            Pageable pageable
    );
}
