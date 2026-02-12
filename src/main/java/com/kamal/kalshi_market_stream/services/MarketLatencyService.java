package com.kamal.kalshi_market_stream.services;

import com.kamal.kalshi_market_stream.DTOs.MarketLatencyResponseDTO;
import com.kamal.kalshi_market_stream.entities.MarketSnapshot;
import com.kamal.kalshi_market_stream.entities.MarketSnapshotLatency;
import com.kamal.kalshi_market_stream.repositories.MarketSnapshotLatencyRepository;
import com.kamal.kalshi_market_stream.repositories.MarketSnapshotRepository;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class MarketLatencyService {

    private final MarketSnapshotLatencyRepository latencyRepository;
    private final MarketSnapshotRepository snapshotRepository;

    public MarketLatencyService(MarketSnapshotLatencyRepository latencyRepository, MarketSnapshotRepository snapshotRepository) {
        this.latencyRepository = latencyRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public void recordLatency(
            MarketSnapshot snapshot,
            Instant exchangeTs,
            Instant receivedTs,
            Instant processedTs,
            String source) {
        MarketSnapshotLatency l = new MarketSnapshotLatency();
        l.setSnapshot(snapshot);
        l.setExchangeTs(exchangeTs);
        l.setReceivedTs(receivedTs);
        l.setProcessedTs(processedTs);

        long networkMs = Duration.between(exchangeTs, receivedTs).toMillis();
        long processingMs = Duration.between(receivedTs, processedTs).toMillis();
        long e2eMs = Duration.between(exchangeTs, processedTs).toMillis();

        l.setNetworkLatencyMs(networkMs);
        l.setProcessingLatencyMs(processingMs);
        l.setEndToEndLatencyMs(e2eMs);

        l.setSource(source);

        latencyRepository.save(l);
    }

    public List<MarketLatencyResponseDTO> getRecentFreshLatencies(String marketTicker, int limit) {

        int safeLimit = Math.max(1, Math.min(limit, 500)); 

        List<MarketSnapshot> recentSnapshots = snapshotRepository.findByMarket_MarketTickerOrderByObservedAtDesc(
                marketTicker,
                PageRequest.of(0, safeLimit)
        );

        Collections.reverse(recentSnapshots);

        // 2) Filter out duplicates by observedAt (exchangeTs / observedAt). Keep first
        // occurrence only.
        Set<Instant> seenObservedAt = new HashSet<>();
        List<Long> snapshotIds = new ArrayList<>();

        for (MarketSnapshot s : recentSnapshots) {
            Instant observedAt = s.getObservedAt(); // your snapshot column storing exchange updated time
            if (observedAt == null)
                continue;

            if (seenObservedAt.add(observedAt)) {
                snapshotIds.add(s.getId());
            }
        }

        if (snapshotIds.isEmpty())
            return Collections.emptyList();

        // 3) Fetch latencies for those snapshot IDs
        List<MarketSnapshotLatency> latencies = latencyRepository.findBySnapshot_IdIn(snapshotIds);

        // 5) Return DTOs in the same order as filtered snapshots
        List<MarketLatencyResponseDTO> result = new ArrayList<>();
        for (MarketSnapshotLatency snapshotId : latencies) {

            result.add(new MarketLatencyResponseDTO(
                    snapshotId.getId(),
                    snapshotId.getExchangeTs(),
                    snapshotId.getReceivedTs(),
                    snapshotId.getProcessedTs(),
                    snapshotId.getNetworkLatencyMs(),
                    snapshotId.getProcessingLatencyMs(),
                    snapshotId.getEndToEndLatencyMs()));
        }

        return result;
    }

}
