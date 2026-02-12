package com.kamal.kalshi_market_stream.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class MarketLatencyResponseDTO {

    private Long snapshotId;
    private Instant exchangeTs;
    private Instant receivedTs;
    private Instant processedTs;
    private long networkLatencyMs;
    private long processingLatencyMs;
    private long endToEndLatencyMs;

}
