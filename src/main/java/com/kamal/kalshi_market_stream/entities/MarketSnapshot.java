package com.kamal.kalshi_market_stream.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Setter
@Getter
@Table(
    name = "market_snapshot"
)
public class MarketSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "market_id", nullable = false)
    private Market market;

    @Column(nullable = false)
    private Instant observedAt; // when your scheduler polled

    // Store integer cents (matches response_price_units=usd_cent)
    private Integer yesBid;
    private Integer yesAsk;
    private Integer noBid;
    private Integer noAsk;
    private Integer lastPrice;

    // Optional but handy
    private Integer volume24h;
    private Integer openInterest;

}
