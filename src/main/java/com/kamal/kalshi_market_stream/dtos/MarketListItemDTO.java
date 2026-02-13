package com.kamal.kalshi_market_stream.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MarketListItemDTO {

    private String marketTicker;
    private String title;
    private String subtitle;
    private String status;
}
