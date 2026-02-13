package com.kamal.kalshi_market_stream.services;

import com.kamal.kalshi_market_stream.dtos.MarketLatencyResponseDTO;
import com.kamal.kalshi_market_stream.entities.MarketSnapshot;
import com.kamal.kalshi_market_stream.entities.MarketSnapshotLatency;
import com.kamal.kalshi_market_stream.repositories.MarketSnapshotLatencyRepository;
import com.kamal.kalshi_market_stream.repositories.MarketSnapshotRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class MarketLatencyService {

    private static final Logger log = LoggerFactory.getLogger(MarketLatencyService.class);

    private final MarketSnapshotLatencyRepository latencyRepository;
    private final MarketSnapshotRepository snapshotRepository;

    public MarketLatencyService(
            MarketSnapshotLatencyRepository latencyRepository,
            MarketSnapshotRepository snapshotRepository
    ) {
        this.latencyRepository = latencyRepository;
        this.snapshotRepository = snapshotRepository;
    }

    public void recordLatency(
            MarketSnapshot snapshot,
            Instant exchangeTs,
            Instant receivedTs,
            Instant processedTs,
            String source
    ) {
        if (snapshot == null || exchangeTs == null || receivedTs == null || processedTs == null) {
            log.warn("Skipping latency record due to nulls | snapshot={} exchangeTs={} receivedTs={} processedTs={}",
                    snapshot != null ? snapshot.getId() : null, exchangeTs, receivedTs, processedTs);
            return;
        }

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

        // Fetch extra because we might drop duplicates by observedAt
        int fetchSize = safeLimit * 3;

        List<MarketSnapshot> recentSnapshots =
                snapshotRepository.findByMarket_MarketTickerOrderByObservedAtDesc(
                        marketTicker,
                        PageRequest.of(0, fetchSize)
                );

        if (recentSnapshots.isEmpty()) {
            return Collections.emptyList();
        }

        // Reverse so we keep the FIRST observation per observedAt
        Collections.reverse(recentSnapshots);

        Set<Instant> seenObservedAt = new HashSet<>();
        List<Long> snapshotIds = new ArrayList<>();

        for (MarketSnapshot s : recentSnapshots) {
            Instant observedAt = s.getObservedAt();
            if (observedAt == null) continue;

            if (seenObservedAt.add(observedAt)) {
                snapshotIds.add(s.getId());
                if (snapshotIds.size() == safeLimit) break;
            }
        }

        if (snapshotIds.isEmpty()) {
            return Collections.emptyList();
        }

        List<MarketSnapshotLatency> latencies =
                latencyRepository.findBySnapshot_IdIn(snapshotIds);

        // Build response in the same order as snapshotIds (simple scan, still small)
        List<MarketLatencyResponseDTO> result = new ArrayList<>();

        for (Long snapshotId : snapshotIds) {
            MarketSnapshotLatency l = null;

            for (MarketSnapshotLatency candidate : latencies) {
                if (candidate.getSnapshot() != null && snapshotId.equals(candidate.getSnapshot().getId())) {
                    l = candidate;
                    break;
                }
            }

            if (l == null) continue;

            result.add(new MarketLatencyResponseDTO(
                    snapshotId, 
                    l.getExchangeTs(),
                    l.getReceivedTs(),
                    l.getProcessedTs(),
                    l.getNetworkLatencyMs(),
                    l.getProcessingLatencyMs(),
                    l.getEndToEndLatencyMs()
            ));
        }

        return result;
    }
}
