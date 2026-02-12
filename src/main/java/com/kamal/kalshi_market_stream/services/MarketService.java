package com.kamal.kalshi_market_stream.services;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamal.kalshi_market_stream.entities.Market;
import com.kamal.kalshi_market_stream.repositories.MarketRepository;

@Service
public class MarketService {

    private final MarketRepository marketRepository;

    public MarketService(MarketRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    @Transactional
    public Market storeOrUpdateMarket(
            String marketTicker,
            String eventTicker,
            String title,
            String subtitle,
            String responsePriceUnits,
            Instant openTime,
            Instant closeTime,
            Instant expirationTime,
            String status,
            Instant createdTime,
            Instant updatedTime
    ) {
        Optional<Market> existingOpt = marketRepository.findByMarketTicker(marketTicker);
        Market market = existingOpt.orElseGet(Market::new);

        market.setMarketTicker(marketTicker);
        market.setEventTicker(eventTicker);
        market.setTitle(title);
        market.setSubtitle(subtitle);
        market.setResponsePriceUnits(responsePriceUnits);
        market.setOpenTime(openTime);
        market.setCloseTime(closeTime);
        market.setExpirationTime(expirationTime);
        market.setStatus(status);
        market.setCreatedTime(createdTime);
        market.setUpdatedTime(updatedTime);

        return marketRepository.save(market);
    }
}
