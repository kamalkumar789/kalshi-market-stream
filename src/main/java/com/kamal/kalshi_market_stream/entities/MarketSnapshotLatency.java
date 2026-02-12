package com.kamal.kalshi_market_stream.entities;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "market_snapshot_latency")
public class MarketSnapshotLatency {

  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(optional = false)
  @JoinColumn(name = "snapshot_id", nullable = false, unique = true)
  private MarketSnapshot snapshot;

  private Instant exchangeTs;
  private Instant receivedTs;
  private Instant processedTs;

  private long networkLatencyMs;
  private long processingLatencyMs;
  private long endToEndLatencyMs;

  private String source;

}
