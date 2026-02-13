package com.kamal.kalshi_market_stream.services;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kamal.kalshi_market_stream.entities.Event;
import com.kamal.kalshi_market_stream.repositories.EventRepository;

@Service
public class EventService {

    private final EventRepository repo;

    public EventService(EventRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public Event storeEventOrUpdate(
            String eventTicker,
            String seriesTicker,
            String title,
            String subTitle,
            Instant time
    ) {
        Optional<Event> opt = repo.findByEventTicker(eventTicker);
        Event e = opt.orElseGet(Event::new);

        e.setEventTicker(eventTicker);
        e.setSeriesTicker(seriesTicker);
        e.setTitle(title);
        e.setSubTitle(subTitle);    
        e.setCreatedAt(time);


        return repo.save(e);
    }
}
