package com.fxhedgedesk.service;

import com.fxhedgedesk.domain.*;
import com.fxhedgedesk.repository.ExposureRepository;
import com.fxhedgedesk.repository.ForwardContractRepository;
import com.fxhedgedesk.service.simulation.FxRateSimulationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PortfolioServiceTest {

    private final AppUser user = new AppUser("desk@example.com", "hash", "Desk");
    private final CurrencyPair eurUsd = new CurrencyPair("EURUSD", "EUR", "USD", new BigDecimal("1.08"),
            new BigDecimal("0.08"), BigDecimal.ZERO);

    private ExposureRepository exposureRepository;
    private ForwardContractRepository forwardContractRepository;
    private FxRateSimulationService rateService;
    private PortfolioService portfolioService;

    @BeforeEach
    void setUp() {
        exposureRepository = mock(ExposureRepository.class);
        forwardContractRepository = mock(ForwardContractRepository.class);
        rateService = mock(FxRateSimulationService.class);
        HedgeService hedgeService = mock(HedgeService.class);
        portfolioService = new PortfolioService(exposureRepository, forwardContractRepository, rateService, hedgeService);

        when(rateService.getCurrentRate("EURUSD")).thenReturn(new BigDecimal("1.10"));
        when(forwardContractRepository.findByUserOrderByCreatedAtDesc(user)).thenReturn(List.of());
    }

    @Test
    void hedgeRatioAndVarAreComputedAcrossAllOpenExposuresOnAPair() {
        Exposure first = new Exposure(user, eurUsd, ExposureDirection.RECEIVABLE, new BigDecimal("10000"),
                new BigDecimal("1.05"), 30, "Invoice A");
        first.applyHedge(new BigDecimal("4000"));

        Exposure second = new Exposure(user, eurUsd, ExposureDirection.PAYABLE, new BigDecimal("5000"),
                new BigDecimal("1.07"), 45, "Bill B");
        second.applyHedge(new BigDecimal("1000"));

        when(exposureRepository.findByUserOrderByDueSimDayAsc(user)).thenReturn(List.of(first, second));

        PortfolioSummary summary = portfolioService.summarize(user);

        assertThat(summary.totalExposureUsd()).isEqualByComparingTo("16500.00");
        assertThat(summary.totalHedgedUsd()).isEqualByComparingTo("5500.00");
        assertThat(summary.hedgeRatio()).isEqualByComparingTo("0.3333");
        assertThat(summary.portfolioVar95Usd()).isEqualByComparingTo("75.77");
        assertThat(summary.byCurrency()).hasSize(1);
        assertThat(summary.byCurrency().get(0).unhedgedUsd()).isEqualByComparingTo("11000.00");
    }

    @Test
    void settledExposuresAreExcludedFromTheLiveRiskPicture() {
        Exposure settled = new Exposure(user, eurUsd, ExposureDirection.RECEIVABLE, new BigDecimal("10000"),
                new BigDecimal("1.05"), 5, "Already settled");
        settled.settle(new BigDecimal("1.09"), new BigDecimal("400.00"));

        when(exposureRepository.findByUserOrderByDueSimDayAsc(user)).thenReturn(List.of(settled));

        PortfolioSummary summary = portfolioService.summarize(user);

        assertThat(summary.totalExposureUsd()).isEqualByComparingTo("0.00");
        assertThat(summary.byCurrency()).isEmpty();
    }

    @Test
    void noExposuresMeansAZeroedOutSummaryNotAnError() {
        when(exposureRepository.findByUserOrderByDueSimDayAsc(user)).thenReturn(List.of());

        PortfolioSummary summary = portfolioService.summarize(user);

        assertThat(summary.hedgeRatio()).isEqualByComparingTo("0");
        assertThat(summary.portfolioVar95Usd()).isEqualByComparingTo("0.00");
    }
}
