package com.kamal.kalshi_market_stream.repositories;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kamal.kalshi_market_stream.entities.MarketSnapshot;

@Repository
public interface MarketSnapshotRepository extends JpaRepository<MarketSnapshot, Long> {
    
    List<MarketSnapshot> findByMarket_MarketTickerAndObservedAtBetweenOrderByObservedAtAsc(
            String marketTicker,
            Instant from,
            Instant to
    );
}
