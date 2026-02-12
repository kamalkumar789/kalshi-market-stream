package com.kamal.kalshi_market_stream.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kamal.kalshi_market_stream.entities.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByEventTicker(String eventTicker);

    boolean existsByEventTicker(String eventTicker);
}