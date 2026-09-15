package com.fxhedgedesk.web.dto;

import com.fxhedgedesk.domain.CurrencyPair;

import java.math.BigDecimal;

public class MarketDtos {

    public record CurrencyPairResponse(String code, String baseCcy, String quoteCcy, BigDecimal currentRate) {
        public static CurrencyPairResponse from(CurrencyPair pair, BigDecimal currentRate) {
            return new CurrencyPairResponse(pair.getCode(), pair.getBaseCcy(), pair.getQuoteCcy(), currentRate);
        }
    }

    public record ClockResponse(long currentSimDay) {
    }
}
