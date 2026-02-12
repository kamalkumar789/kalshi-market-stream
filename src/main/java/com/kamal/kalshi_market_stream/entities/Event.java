package com.kamal.kalshi_market_stream.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "events", uniqueConstraints = {
        @UniqueConstraint(columnNames = "event_ticker")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_ticker", nullable = false, unique = true)
    private String eventTicker;

    @Column(name = "series_ticker", nullable = false)
    private String seriesTicker;

    private String title;

    @Column(name = "sub_title")
    private String subTitle;

     @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Market> markets;
}
