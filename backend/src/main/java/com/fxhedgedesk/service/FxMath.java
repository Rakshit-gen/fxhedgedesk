package com.fxhedgedesk.service;

import com.fxhedgedesk.domain.CurrencyPair;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

/**
 * The one place that knows how to turn a foreign-currency amount into USD.
 * A pair like EURUSD quotes USD per unit of the base currency, so USD value
 * is amount times rate. A pair like USDJPY quotes the foreign currency per
 * USD, so it's amount divided by rate. Every service that touches money
 * across a pair goes through here instead of re-deriving this each time.
 */
public final class FxMath {

    private static final MathContext MC = new MathContext(12);

    private FxMath() {
    }

    public static boolean isUsdBase(CurrencyPair pair) {
        return "USD".equals(pair.getBaseCcy());
    }

    /** The non-USD side of the pair, the currency an exposure is actually denominated in. */
    public static String foreignCurrency(CurrencyPair pair) {
        return isUsdBase(pair) ? pair.getQuoteCcy() : pair.getBaseCcy();
    }

    public static BigDecimal toUsd(CurrencyPair pair, BigDecimal foreignAmount, BigDecimal rate) {
        BigDecimal value = isUsdBase(pair)
                ? foreignAmount.divide(rate, MC)
                : foreignAmount.multiply(rate, MC);
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
