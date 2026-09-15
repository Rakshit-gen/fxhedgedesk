package com.fxhedgedesk.service;

import java.math.BigDecimal;
import java.util.List;

public record PortfolioSummary(
        BigDecimal totalExposureUsd,
        BigDecimal totalHedgedUsd,
        BigDecimal hedgeRatio,
        BigDecimal portfolioVar95Usd,
        BigDecimal openForwardsMtmUsd,
        BigDecimal realizedPnlUsd,
        List<CurrencyBreakdown> byCurrency
) {
    public record CurrencyBreakdown(
            String pairCode,
            BigDecimal totalUsd,
            BigDecimal hedgedUsd,
            BigDecimal unhedgedUsd,
            BigDecimal hedgeRatio,
            BigDecimal var95Usd
    ) {
    }
}
