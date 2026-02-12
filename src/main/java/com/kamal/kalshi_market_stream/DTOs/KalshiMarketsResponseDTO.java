package com.kamal.kalshi_market_stream.DTOs;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class KalshiMarketsResponseDTO {

    private List<MarketDTO> markets;
    private String cursor;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MarketDTO {

        private String ticker;

        @JsonProperty("event_ticker")
        private String eventTicker;

        @JsonProperty("market_type")
        private String marketType;

        private String title;
        private String subtitle;

        @JsonProperty("yes_sub_title")
        private String yesSubTitle;

        @JsonProperty("no_sub_title")
        private String noSubTitle;

        @JsonProperty("created_time")
        private String createdTime;

        @JsonProperty("updated_time")
        private String updatedTime;

        @JsonProperty("open_time")
        private String openTime;

        @JsonProperty("close_time")
        private String closeTime;

        @JsonProperty("expiration_time")
        private String expirationTime;

        @JsonProperty("latest_expiration_time")
        private String latestExpirationTime;

        @JsonProperty("settlement_timer_seconds")
        private Integer settlementTimerSeconds;

        private String status;

        @JsonProperty("response_price_units")
        private String responsePriceUnits;

        @JsonProperty("yes_bid")
        private Integer yesBid;

        @JsonProperty("yes_bid_dollars")
        private String yesBidDollars;

        @JsonProperty("yes_ask")
        private Integer yesAsk;

        @JsonProperty("yes_ask_dollars")
        private String yesAskDollars;

        @JsonProperty("no_bid")
        private Integer noBid;

        @JsonProperty("no_bid_dollars")
        private String noBidDollars;

        private Integer noAsk;

        @JsonProperty("no_ask_dollars")
        private String noAskDollars;

        @JsonProperty("last_price")
        private Integer lastPrice;
        @JsonProperty("last_price_dollars")
        private String lastPriceDollars;

        private Integer volume;

        @JsonProperty("volume_fp")
        private String volumeFp;

        @JsonProperty("volume_24h")
        private Integer volume24h;

        @JsonProperty("volume_24h_fp")
        private String volume24hFp;

        private String result;

        @JsonProperty("can_close_early")
        private Boolean canCloseEarly;

        @JsonProperty("open_interest")
        private Integer openInterest;

        @JsonProperty("open_interest_fp")
        private String openInterestFp;

        @JsonProperty("notional_value")
        private Integer notionalValue;

        @JsonProperty("notional_value_dollars")
        private String notionalValueDollars;

        @JsonProperty("previous_yes_bid")
        private Integer previousYesBid;

        @JsonProperty("previous_yes_bid_dollars")
        private String previousYesBidDollars;

        @JsonProperty("previous_yes_ask")
        private Integer previousYesAsk;

        @JsonProperty("previous_yes_ask_dollars")
        private String previousYesAskDollars;

        @JsonProperty("previous_price")
        private Integer previousPrice;

        @JsonProperty("previous_price_dollars")
        private String previousPriceDollars;

        private Integer liquidity;

        @JsonProperty("liquidity_dollars")
        private String liquidityDollars;

        @JsonProperty("expiration_value")
        private String expirationValue;

        @JsonProperty("tick_size")
        private Integer tickSize;

        @JsonProperty("rules_primary")
        private String rulesPrimary;

        @JsonProperty("rules_secondary")
        private String rulesSecondary;

        @JsonProperty("price_level_structure")
        private String priceLevelStructure;

        @JsonProperty("price_ranges")
        private List<PriceRangeDTO> priceRanges;

        @JsonProperty("expected_expiration_time")
        private String expectedExpirationTime;

        @JsonProperty("settlement_value")
        private Integer settlementValue;

        @JsonProperty("settlement_value_dollars")
        private String settlementValueDollars;

        @JsonProperty("settlement_ts")
        private String settlementTs;

        @JsonProperty("fee_waiver_expiration_time")
        private String feeWaiverExpirationTime;

        @JsonProperty("early_close_condition")
        private String earlyCloseCondition;

        @JsonProperty("strike_type")
        private String strikeType;

        @JsonProperty("floor_strike")
        private Integer floorStrike;

        @JsonProperty("cap_strike")
        private Integer capStrike;

        @JsonProperty("functional_strike")
        private String functionalStrike;

        @JsonProperty("custom_strike")
        private Map<String, Object> customStrike;

        @JsonProperty("mve_collection_ticker")
        private String mveCollectionTicker;

        @JsonProperty("mve_selected_legs")
        private List<MveSelectedLegDTO> mveSelectedLegs;

        @JsonProperty("primary_participant_key")
        private String primaryParticipantKey;

        @JsonProperty("is_provisional")
        private Boolean isProvisional;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PriceRangeDTO {
        private String start;
        private String end;
        private String step;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MveSelectedLegDTO {

        @JsonProperty("event_ticker")
        private String eventTicker;

        @JsonProperty("market_ticker")
        private String marketTicker;

        private String side;

        @JsonProperty("yes_settlement_value_dollars")
        private String yesSettlementValueDollars;
    }
}
