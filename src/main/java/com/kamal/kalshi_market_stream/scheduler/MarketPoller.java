package com.kamal.kalshi_market_stream.scheduler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kamal.kalshi_market_stream.DTOs.KalshiEventResponseDTO;
import com.kamal.kalshi_market_stream.DTOs.KalshiEventResponseDTO.EventDTO;
import com.kamal.kalshi_market_stream.DTOs.KalshiEventResponseDTO.MarketDTO;
import com.kamal.kalshi_market_stream.client.KalshiClient;
import com.kamal.kalshi_market_stream.entities.Event;
import com.kamal.kalshi_market_stream.entities.Market;
import com.kamal.kalshi_market_stream.entities.MarketSnapshot;
import com.kamal.kalshi_market_stream.services.EventService;
import com.kamal.kalshi_market_stream.services.MarketLatencyService;
import com.kamal.kalshi_market_stream.services.MarketService;
import com.kamal.kalshi_market_stream.services.MarketSnapshotService;
import com.kamal.kalshi_market_stream.utils.Signals;
import com.kamal.kalshi_market_stream.utils.SignalsEngine;

@Service
public class MarketPoller {

    private static final Logger log = LoggerFactory.getLogger(MarketPoller.class);

    @Value("${app.timezone}")
    private String appTz;

    private final List<String> seriesTickers;

    private static final DateTimeFormatter EVENT_DATE_FMT = DateTimeFormatter.ofPattern("yyMMMdd", Locale.ENGLISH);

    private final ConcurrentMap<String, Boolean> inFlightSeries = new ConcurrentHashMap<>();

    private final Executor marketPollExecutor;
    private final KalshiClient client;

    private final EventService eventService;
    private final MarketService marketService;
    private final MarketSnapshotService snapshotService;
    private final MarketLatencyService latencyService;
    private final SignalsEngine signalsEngine;

    public MarketPoller(
            KalshiClient client,
            EventService eventService,
            MarketService marketService,
            MarketSnapshotService snapshotService,
            MarketLatencyService latencyService,
            SignalsEngine signalsEngine,
            Executor marketPollExecutor,
            @Value("${kalshi.poll.seriesTickers}") List<String> seriesTickers) {
        this.client = client;
        this.eventService = eventService;
        this.marketService = marketService;
        this.snapshotService = snapshotService;
        this.latencyService = latencyService;
        this.signalsEngine = signalsEngine;
        this.marketPollExecutor = marketPollExecutor;
        this.seriesTickers = seriesTickers;
    }

    @Scheduled(fixedRate = 5000)
    public void poll() {
        for (String series : seriesTickers) {
            marketPollExecutor.execute(() -> pollSeriesSafe(series));
        }
    }

    private void pollSeriesSafe(String seriesTicker) {
        if (inFlightSeries.putIfAbsent(seriesTicker, true) != null)
            return;

        try {
            pollSeries(seriesTicker);
        } catch (Exception e) {
            log.warn("Poll failed | series={} err={}", seriesTicker, e.toString());
        } finally {
            inFlightSeries.remove(seriesTicker);
        }
    }

    private void pollSeries(String seriesTicker) {

        ZoneId Zone = ZoneId.of(appTz);
        String todayPart = LocalDate.now(Zone)
                .format(EVENT_DATE_FMT)
                .toUpperCase(Locale.ENGLISH); // 26FEB12


        String eventTicker = seriesTicker + "-" + todayPart;

        KalshiEventResponseDTO resp = client.getEvent(eventTicker);
        if (resp == null || resp.getEvent() == null || resp.getMarkets() == null) {
            log.info("No data returned | event={}", eventTicker);
            return;
        }

        // y: when API response is received by your server
        Instant receivedTs = Instant.now();

        // 1) Upsert EVENT first
        EventDTO e = resp.getEvent();
        Event event = eventService.storeEventOrUpdate(
                e.getEvent_ticker(),
                e.getSeries_ticker(),
                e.getTitle(),
                e.getSub_title()

        );

        // 2) Upsert MARKET(s) linked to EVENT, then 3) snapshot + latency + signal
        for (MarketDTO dto : resp.getMarkets()) {
            
                preDbLog(seriesTicker, eventTicker, e, dto);

            Instant exchangeTs = dto.getUpdated_time(); // x

            Market market = marketService.storeOrUpdateMarket(
                    event,
                    dto.getTicker(),
                    dto.getTitle(),
                    dto.getSubtitle(),
                    dto.getOpen_time(),
                    dto.getClose_time(),
                    dto.getStatus(),
                    dto.getCreated_time(),
                    dto.getUpdated_time());

            if (market.getResult() != null && !market.getResult().isEmpty()) {
                signalsEngine.remove(event.getEventTicker());
                continue;
            }

            MarketSnapshot snapshot = snapshotService.storeSnapshot(
                    market,
                    exchangeTs,
                    dto.getYes_bid(),
                    dto.getYes_ask(),
                    dto.getNo_bid(),
                    dto.getNo_ask(),
                    dto.getLast_price());

            Instant processedTs = Instant.now(); // z

            latencyService.recordLatency(
                    snapshot,
                    exchangeTs,
                    receivedTs,
                    processedTs,
                    "REST");

            int signalPrice = computeYesMid(dto);
            Signals.Trend trend = signalsEngine.update(event.getEventTicker(), signalPrice);

            log.info("Poll | series={} event={} market={} price={} trend={}",
                    seriesTicker, event.getEventTicker(), dto.getTicker(), signalPrice, trend);
        }
    }

    private int computeYesMid(MarketDTO dto) {
        Integer bid = dto.getYes_bid();
        Integer ask = dto.getYes_ask();
        if (bid != null && ask != null)
            return (bid + ask) / 2;
        if (ask != null)
            return ask;
        if (bid != null)
            return bid;
        return 0;
    }

    private void preDbLog(
            String seriesTicker,
            String eventTicker,
            KalshiEventResponseDTO.EventDTO e,
            KalshiEventResponseDTO.MarketDTO m) {
        log.info("""

                ===================[ PRE-DB MARKET SNAPSHOT ]===================
                SeriesTicker : {}
                EventTicker  : {}
                EventTitle   : {}
                MarketTicker : {}
                MarketTitle  : {}
                Status       : {}
                ----------------------------------------------------------------
                YES  -> bid:{} ask:{}
                NO   -> bid:{} ask:{}
                =================================================================
                """,
                seriesTicker,
                eventTicker,
                safeStr(e.getTitle()),
                safeStr(m.getTicker()),
                safeStr(m.getTitle()),
                safeStr(m.getStatus()),
                safeInt(m.getYes_bid()), safeInt(m.getYes_ask()),
                safeInt(m.getNo_bid()), safeInt(m.getNo_ask())
                );
    }

    private String safeStr(String s) {
        return (s == null || s.isBlank()) ? "-" : s;
    }

    private int safeInt(Integer v) {
        return v == null ? -1 : v;
    }

}
