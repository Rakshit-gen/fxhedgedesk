package com.fxhedgedesk.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ExposureTest {

    private final CurrencyPair pair = new CurrencyPair("EURUSD", "EUR", "USD", BigDecimal.ONE,
            new BigDecimal("0.08"), BigDecimal.ZERO);
    private final AppUser user = new AppUser("desk@example.com", "hash", "Desk");

    @Test
    void partialHedgeMovesStatusToPartiallyHedged() {
        Exposure exposure = new Exposure(user, pair, ExposureDirection.RECEIVABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Q1 invoice");

        exposure.applyHedge(new BigDecimal("4000"));

        assertThat(exposure.getStatus()).isEqualTo(ExposureStatus.PARTIALLY_HEDGED);
        assertThat(exposure.remainingToHedge()).isEqualByComparingTo("6000");
    }

    @Test
    void fullyHedgingMovesStatusToHedged() {
        Exposure exposure = new Exposure(user, pair, ExposureDirection.RECEIVABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Q1 invoice");

        exposure.applyHedge(new BigDecimal("10000"));

        assertThat(exposure.getStatus()).isEqualTo(ExposureStatus.HEDGED);
        assertThat(exposure.isFullyHedged()).isTrue();
    }

    @Test
    void hedgingMoreThanTheRemainingAmountIsRejected() {
        Exposure exposure = new Exposure(user, pair, ExposureDirection.PAYABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Supplier bill");
        exposure.applyHedge(new BigDecimal("7000"));

        assertThatThrownBy(() -> exposure.applyHedge(new BigDecimal("4000")))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void settlingTwiceIsRejected() {
        Exposure exposure = new Exposure(user, pair, ExposureDirection.PAYABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Supplier bill");
        exposure.settle(new BigDecimal("1.10"), new BigDecimal("-200"));

        assertThatThrownBy(() -> exposure.settle(new BigDecimal("1.10"), BigDecimal.ZERO))
                .isInstanceOf(IllegalStateException.class);
    }
}
