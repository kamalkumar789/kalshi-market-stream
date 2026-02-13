package com.kamal.kalshi_market_stream.repositories;

import com.kamal.kalshi_market_stream.entities.MarketSnapshot;
import com.kamal.kalshi_market_stream.entities.MarketSnapshotLatency;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketSnapshotLatencyRepository extends JpaRepository<MarketSnapshotLatency, Long> {

    List<MarketSnapshotLatency> findBySnapshotIn(List<MarketSnapshot> marketSnapshots);

}
