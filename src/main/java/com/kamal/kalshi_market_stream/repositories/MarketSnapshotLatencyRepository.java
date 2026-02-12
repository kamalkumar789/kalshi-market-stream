package com.kamal.kalshi_market_stream.repositories;

import com.kamal.kalshi_market_stream.entities.MarketSnapshotLatency;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface MarketSnapshotLatencyRepository extends JpaRepository<MarketSnapshotLatency, Long> {

    List<MarketSnapshotLatency> findBySnapshot_IdIn(List<Long> snapshotIds);

}
