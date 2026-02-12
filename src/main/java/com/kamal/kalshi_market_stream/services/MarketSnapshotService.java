package com.kamal.kalshi_market_stream.services;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
            Integer openInterest
    ) {
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
    

    public List<MarketSnapshotPointDTO> fetch(String marketTicker, Instant from, Instant to) {

        System.out.println(from + " "+ to);
        return snapshotRepository
                .findByMarket_MarketTickerAndObservedAtBetweenOrderByObservedAtAsc(marketTicker, from, to)
                .stream()
                .map(s -> new MarketSnapshotPointDTO(
                        s.getObservedAt(),
                        s.getYesBid(),
                        s.getNoBid(),
                        s.getLastPrice(),
                        s.getMarket().getSubtitle()
                ))
                .collect(Collectors.toList());
    }

}
