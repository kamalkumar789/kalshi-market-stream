package com.kamal.kalshi_market_stream.services;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamal.kalshi_market_stream.dtos.MarketListItemDTO;
import com.kamal.kalshi_market_stream.entities.Event;
import com.kamal.kalshi_market_stream.entities.Market;
import com.kamal.kalshi_market_stream.repositories.EventRepository;
import com.kamal.kalshi_market_stream.repositories.MarketRepository;

@Service
public class MarketService {

    private final MarketRepository marketRepository;
    private final EventRepository eventRepository;

    private static final Logger log = LoggerFactory.getLogger(MarketService.class);

    public MarketService(MarketRepository marketRepository, EventRepository eventRepository) {
        this.eventRepository = eventRepository;
        this.marketRepository = marketRepository;
    }

    @Transactional
    public Market storeOrUpdateMarket(
            Event event,
            String marketTicker,
            String title,
            String subtitle,
            Instant openTime,
            Instant closeTime,
            String status,
            Instant createdTime,
            Instant updatedTime) {

        Optional<Market> existingOpt = marketRepository.findByMarketTicker(marketTicker);

        Market market;
        if (existingOpt.isPresent()) {
            market = existingOpt.get();
        } else {
            market = new Market();
        }

        market.setEvent(event);
        market.setTitle(title);
        market.setMarketTicker(marketTicker);
        market.setSubtitle(subtitle);
        market.setOpenTime(openTime);
        market.setCloseTime(closeTime);
        market.setStatus(status);
        market.setCreatedTime(createdTime);
        market.setUpdatedTime(updatedTime);

        Market saved = marketRepository.save(market);


        return saved;
    }

    public List<MarketListItemDTO> listMarketsByEvent(String eventTicker, String status) {
        try {
            Event event = eventRepository.findByEventTicker(eventTicker)
                    .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventTicker));

            List<Market> markets = (status == null || status.isBlank())
                    ? marketRepository.findByEventIdOrderByMarketTickerAsc(event.getId())
                    : marketRepository.findByEventIdAndStatusOrderByMarketTickerAsc(event.getId(), status);

            return markets.stream()
                    .map(m -> new MarketListItemDTO(
                            m.getMarketTicker(),
                            m.getTitle(), 
                            m.getSubtitle(),
                            m.getStatus()))
                    .toList();

        } catch (Exception e) {
            log.error("listMarketsByEvent failed: eventTicker={}", eventTicker, e);
            return Collections.emptyList();
        }
    }
}
