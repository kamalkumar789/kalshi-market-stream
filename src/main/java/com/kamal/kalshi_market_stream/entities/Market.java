package com.kamal.kalshi_market_stream.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Setter
@Getter
@Entity
@Table(
    name = "market"
)
public class Market {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String marketTicker;

    @Column(nullable = false, length = 64)
    private String eventTicker;

    @Column(length = 256)
    private String title;

    @Column(length = 256)
    private String subtitle;

    @Column(length = 32)
    private String responsePriceUnits; // e.g. "usd_cent"

    private Instant openTime;
    private Instant closeTime;
    private Instant expirationTime;

    @Column(length = 32)
    private String status;

    private Instant createdTime;
    private Instant updatedTime;

    private String result;

}
