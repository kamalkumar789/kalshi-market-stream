package com.kamal.kalshi_market_stream.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Setter
@Getter
@Entity
@Table(name = "market")
public class Market {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String marketTicker;

    // ✅ Many markets -> one event
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(length = 256)
    private String title;

    @Column(length = 256)
    private String subtitle;

    private Instant openTime;
    private Instant closeTime;
    private Instant expirationTime;

    @Column(length = 32)
    private String status;

    private Instant createdTime;
    private Instant updatedTime;

    private String result;
}
