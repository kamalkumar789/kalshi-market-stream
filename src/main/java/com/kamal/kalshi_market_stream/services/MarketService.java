package com.kamal.kalshi_market_stream.services;

import java.time.Instant;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamal.kalshi_market_stream.entities.Market;
import com.kamal.kalshi_market_stream.repositories.MarketRepository;

@Service
public class MarketService {

    private final MarketRepository marketRepository;
    private static final Logger log = LoggerFactory.getLogger(MarketService.class);

    public MarketService(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    @Transactional
    public Market storeOrUpdateMarket(
            String marketTicker,
            String eventTicker,
            String title,
            String subtitle,
            Instant openTime,
            Instant closeTime,
            String status,
            Instant createdTime,
            Instant updatedTime
    ) {
        log.info("storeOrUpdateMarket called for marketTicker={}", marketTicker);

        Optional<Market> existingOpt = marketRepository.findByMarketTicker(marketTicker);

        Market market;
        if (existingOpt.isPresent()) {
            log.info("Updating existing market: {}", marketTicker);
            market = existingOpt.get();
        } else {
            log.info("Creating new market: {}", marketTicker);
            market = new Market();
        }

        market.setMarketTicker(marketTicker);
        market.setEventTicker(eventTicker);
        market.setTitle(title);
        market.setSubtitle(subtitle);
        market.setOpenTime(openTime);
        market.setCloseTime(closeTime);
        market.setStatus(status);
        market.setCreatedTime(createdTime);
        market.setUpdatedTime(updatedTime);

        Market saved = marketRepository.save(market);

        log.info("Market saved successfully: marketTicker={}, id={}", 
                 saved.getMarketTicker(), saved.getId());

        return saved;
    }
}
