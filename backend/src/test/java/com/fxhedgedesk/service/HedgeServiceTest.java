package com.fxhedgedesk.service;

import com.fxhedgedesk.domain.*;
import com.fxhedgedesk.repository.ExposureRepository;
import com.fxhedgedesk.repository.ForwardContractRepository;
import com.fxhedgedesk.service.simulation.FxRateSimulationService;
import com.fxhedgedesk.service.simulation.SimulationClockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class HedgeServiceTest {

    private final AppUser user = new AppUser("desk@example.com", "hash", "Desk");
    private final CurrencyPair eurUsd = new CurrencyPair("EURUSD", "EUR", "USD", new BigDecimal("1.08"),
            new BigDecimal("0.08"), BigDecimal.ZERO);

    private ExposureService exposureService;
    private ExposureRepository exposureRepository;
    private ForwardContractRepository forwardContractRepository;
    private FxRateSimulationService rateService;
    private SimulationClockService clockService;
    private HedgeService hedgeService;

    @BeforeEach
    void setUp() {
        exposureService = mock(ExposureService.class);
        exposureRepository = mock(ExposureRepository.class);
        forwardContractRepository = mock(ForwardContractRepository.class);
        rateService = mock(FxRateSimulationService.class);
        clockService = mock(SimulationClockService.class);
        hedgeService = new HedgeService(exposureService, exposureRepository, forwardContractRepository, rateService, clockService);

        when(forwardContractRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(clockService.currentSimDay()).thenReturn(10L);
    }

    @Test
    void hedgingAReceivableSellsTheBaseCurrencyForward() {
        Exposure exposure = new Exposure(user, eurUsd, ExposureDirection.RECEIVABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Invoice");
        when(exposureService.requireOwnedBy(exposure.getId(), user)).thenReturn(exposure);
        when(rateService.getCurrentRate("EURUSD")).thenReturn(new BigDecimal("1.09"));

        ForwardContract forward = hedgeService.bookForward(user, exposure.getId(), new BigDecimal("4000"));

        assertThat(forward.getDirection()).isEqualTo(ForwardDirection.SELL);
        assertThat(forward.getContractedRate()).isEqualByComparingTo("1.09");
        assertThat(exposure.getHedgedAmount()).isEqualByComparingTo("4000");
    }

    @Test
    void hedgingAPayableBuysTheBaseCurrencyForward() {
        Exposure exposure = new Exposure(user, eurUsd, ExposureDirection.PAYABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Bill");
        when(exposureService.requireOwnedBy(exposure.getId(), user)).thenReturn(exposure);
        when(rateService.getCurrentRate("EURUSD")).thenReturn(new BigDecimal("1.09"));

        ForwardContract forward = hedgeService.bookForward(user, exposure.getId(), new BigDecimal("4000"));

        assertThat(forward.getDirection()).isEqualTo(ForwardDirection.BUY);
    }

    @Test
    void unrealizedPnlOnASellForwardIsPositiveWhenSpotFellBelowTheContractedRate() {
        Exposure exposure = new Exposure(user, eurUsd, ExposureDirection.RECEIVABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Invoice");
        ForwardContract forward = new ForwardContract(user, exposure, eurUsd, new BigDecimal("5000"),
                new BigDecimal("1.10"), ForwardDirection.SELL, 5, 30);

        when(rateService.getCurrentRate("EURUSD")).thenReturn(new BigDecimal("1.05"));

        BigDecimal pnl = hedgeService.unrealizedPnl(forward);

        // Locked in selling at 1.10, spot is now 1.05: better than the market, a gain.
        assertThat(pnl.signum()).isPositive();
        assertThat(pnl).isEqualByComparingTo("250.00");
    }

    @Test
    void bookingAForwardLargerThanTheRemainingUnhedgedAmountIsRejected() {
        Exposure exposure = new Exposure(user, eurUsd, ExposureDirection.RECEIVABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Invoice");
        when(exposureService.requireOwnedBy(exposure.getId(), user)).thenReturn(exposure);
        when(rateService.getCurrentRate("EURUSD")).thenReturn(new BigDecimal("1.09"));

        assertThatThrownBy(() -> hedgeService.bookForward(user, exposure.getId(), new BigDecimal("15000")))
                .isInstanceOf(IllegalArgumentException.class);

        verify(forwardContractRepository, never()).save(any());
    }

    @Test
    void unrealizedPnlOnASettledForwardIsJustItsRealizedPnl() {
        Exposure exposure = new Exposure(user, eurUsd, ExposureDirection.PAYABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Bill");
        ForwardContract forward = new ForwardContract(user, exposure, eurUsd, new BigDecimal("5000"),
                new BigDecimal("1.10"), ForwardDirection.BUY, 5, 30);
        forward.settle(new BigDecimal("1.12"), new BigDecimal("-99.00"));

        BigDecimal pnl = hedgeService.unrealizedPnl(forward);

        assertThat(pnl).isEqualByComparingTo("-99.00");
        verifyNoInteractions(rateService);
    }
}
