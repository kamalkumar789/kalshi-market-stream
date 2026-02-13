
package com.kamal.kalshi_market_stream.dtos;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MarketSnapshotPointDTO {
    private Instant createdAt;
    private Integer yesBid;
    private Integer noBid;
    private Integer yesAsk;
    private Integer noAsk;
    private String subtitle;
    private String status;
    private String eventTicker; 

}