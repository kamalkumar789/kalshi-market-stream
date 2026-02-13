package com.kamal.kalshi_market_stream.services;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
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
import com.kamal.kalshi_market_stream.entities.Event;
import com.kamal.kalshi_market_stream.entities.Market;
import com.kamal.kalshi_market_stream.entities.MarketSnapshot;
import com.kamal.kalshi_market_stream.repositories.EventRepository;
import com.kamal.kalshi_market_stream.repositories.MarketRepository;
import com.kamal.kalshi_market_stream.repositories.MarketSnapshotRepository;

@Service
public class MarketSnapshotService {

    private final MarketSnapshotRepository snapshotRepository;
    private final EventRepository eventRepository;
        private final MarketRepository marketRepository;


    private static final Logger log = LoggerFactory.getLogger(MarketSnapshotService.class);

    public MarketSnapshotService(MarketSnapshotRepository snapshotRepository, EventRepository eventRepository, MarketRepository marketRepository

) {
        this.snapshotRepository = snapshotRepository;
        this.eventRepository = eventRepository;
        this.marketRepository = marketRepository;
    }

    @Transactional
    public MarketSnapshot storeSnapshot(
            Market market,
            Instant observedAt,
            Integer yesBid,
            Integer yesAsk,
            Integer noBid,
            Integer noAsk,
            Integer lastPrice) {

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

    public List<MarketSnapshotPointDTO> fetchByEventAndMarket(
            String eventTicker,
            String marketTicker,
            String status, // belongs to Market
            LocalDateTime from,
            LocalDateTime to,
            int limit) {

        int safeLimit = Math.max(1, Math.min(limit, 5000));
        ZoneId zone = ZoneId.of("Europe/London"); // or UTC, but be consistent

        try {
            // 1) event exists?
            Event event = eventRepository.findByEventTicker(eventTicker)
                    .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventTicker));

            // 2) market exists + belongs to that event + has correct status (market.status)
            Market market = marketRepository
                    .findByMarketTickerAndEventIdAndStatus(marketTicker, event.getId(), status)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Market not found for event/status: market=" + marketTicker +
                                    ", event=" + eventTicker + ", status=" + status));

            // 3) convert local times to Instant for DB filtering (if you store UTC)
            Instant fromInstant = (from == null) ? null : from.atZone(zone).toInstant();
            Instant toInstant = (to == null) ? null : to.atZone(zone).toInstant();

            // 4) snapshots for this market only
            Sort sort = Sort.by(Sort.Direction.DESC, "observedAt");
            Pageable pageable = PageRequest.of(0, safeLimit, sort);

            List<MarketSnapshot> rows;
            if (fromInstant != null && toInstant != null) {
                rows = snapshotRepository.findByMarketIdAndObservedAtBetween(
                        market.getId(), fromInstant, toInstant, pageable);
            } else {
                rows = snapshotRepository.findByMarketId(
                        market.getId(), pageable);
            }

            log.info("Fetched {} snapshots for event={}, market={}, status={}",
                    rows.size(), eventTicker, marketTicker, status);

            return rows.stream()
                    .map(s -> new MarketSnapshotPointDTO(
                            s.getObservedAt(),
                            s.getYesBid(),
                            s.getNoBid(),
                            s.getMarket().getSubtitle(),
                            s.getMarket().getStatus(),
                            s.getMarket().getEvent().getEventTicker()))
                    .toList();

        } catch (Exception e) {
            log.error("fetchByEventAndMarket failed: event={}, market={}", eventTicker, marketTicker, e);
            return Collections.emptyList();
        }
    }
}
