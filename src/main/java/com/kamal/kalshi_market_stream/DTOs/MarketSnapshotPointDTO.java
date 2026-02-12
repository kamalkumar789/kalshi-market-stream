
package com.kamal.kalshi_market_stream.DTOs;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MarketSnapshotPointDTO {
    private Instant observedAt;
    private Integer yesBid;
    private Integer noBid;
    private Integer lastPrice;
    private String subtitle;
}