package com.fxhedgedesk.service;

import com.fxhedgedesk.domain.CurrencyPair;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class FxMathTest {

    private static final BigDecimal VOL = new BigDecimal("0.08");

    @Test
    void convertsToUsdByMultiplyingWhenUsdIsTheQuoteCurrency() {
        CurrencyPair eurUsd = new CurrencyPair("EURUSD", "EUR", "USD", BigDecimal.ONE, VOL, BigDecimal.ZERO);
        BigDecimal usd = FxMath.toUsd(eurUsd, new BigDecimal("1000"), new BigDecimal("1.10"));
        assertThat(usd).isEqualByComparingTo("1100.00");
    }

    @Test
    void convertsToUsdByDividingWhenUsdIsTheBaseCurrency() {
        CurrencyPair usdJpy = new CurrencyPair("USDJPY", "USD", "JPY", BigDecimal.ONE, VOL, BigDecimal.ZERO);
        BigDecimal usd = FxMath.toUsd(usdJpy, new BigDecimal("15000"), new BigDecimal("150"));
        assertThat(usd).isEqualByComparingTo("100.00");
    }

    @Test
    void identifiesForeignCurrencyRegardlessOfWhichSideUsdIsOn() {
        CurrencyPair eurUsd = new CurrencyPair("EURUSD", "EUR", "USD", BigDecimal.ONE, VOL, BigDecimal.ZERO);
        CurrencyPair usdJpy = new CurrencyPair("USDJPY", "USD", "JPY", BigDecimal.ONE, VOL, BigDecimal.ZERO);
        assertThat(FxMath.foreignCurrency(eurUsd)).isEqualTo("EUR");
        assertThat(FxMath.foreignCurrency(usdJpy)).isEqualTo("JPY");
    }
}
