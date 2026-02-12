package com.kamal.kalshi_market_stream.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class SignalsEngine {

    private final ConcurrentMap<String, Signals> signalsByEvent = new ConcurrentHashMap<>();
    private static final Logger log = LoggerFactory.getLogger(SignalsEngine.class);

    public Signals.Trend update(String eventTicker, int price) {
        Signals s = signalsByEvent.computeIfAbsent(
                eventTicker,
                k -> new Signals());

        return s.update(price);
    }

    public void remove(String eventTicker) {
        signalsByEvent.remove(eventTicker);
    }
}
