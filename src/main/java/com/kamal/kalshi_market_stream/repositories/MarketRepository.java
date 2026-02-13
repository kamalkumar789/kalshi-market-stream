package com.kamal.kalshi_market_stream.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kamal.kalshi_market_stream.entities.Market;

@Repository
public interface MarketRepository extends JpaRepository<Market, Long> {
    Optional<Market> findByMarketTicker(String marketTicker);

    Optional<Market> findByMarketTickerAndEventIdAndStatus(
            String marketTicker,
            Long eventId,
            String status
    );

    List<Market> findByEventIdOrderByMarketTickerAsc(Long eventId);

    List<Market> findByEventIdAndStatusOrderByMarketTickerAsc(Long eventId, String status);


}
