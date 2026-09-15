package com.fxhedgedesk.service.simulation;

import com.fxhedgedesk.domain.*;
import com.fxhedgedesk.repository.ExposureRepository;
import com.fxhedgedesk.repository.ForwardContractRepository;
import com.fxhedgedesk.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class SettlementServiceTest {

    private final AppUser user = new AppUser("desk@example.com", "hash", "Desk");
    private final CurrencyPair eurUsd = new CurrencyPair("EURUSD", "EUR", "USD", new BigDecimal("1.08"),
            new BigDecimal("0.08"), BigDecimal.ZERO);

    private ExposureRepository exposureRepository;
    private ForwardContractRepository forwardContractRepository;
    private FxRateSimulationService rateService;
    private WalletService walletService;
    private SettlementService settlementService;

    @BeforeEach
    void setUp() {
        exposureRepository = mock(ExposureRepository.class);
        forwardContractRepository = mock(ForwardContractRepository.class);
        rateService = mock(FxRateSimulationService.class);
        walletService = mock(WalletService.class);
        settlementService = new SettlementService(exposureRepository, forwardContractRepository, rateService, walletService);

        when(exposureRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(forwardContractRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void settlesAnOpenForwardAndPostsItsPnlToTheWallet() {
        Exposure exposure = new Exposure(user, eurUsd, ExposureDirection.RECEIVABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Invoice");
        exposure.applyHedge(new BigDecimal("10000"));
        ForwardContract forward = new ForwardContract(user, exposure, eurUsd, new BigDecimal("10000"),
                new BigDecimal("1.10"), ForwardDirection.SELL, 5, 30);

        when(exposureRepository.findByStatusNotAndDueSimDayLessThanEqual(ExposureStatus.SETTLED, 30L))
                .thenReturn(List.of(exposure));
        when(forwardContractRepository.findByExposure(exposure)).thenReturn(List.of(forward));
        when(rateService.getCurrentRate("EURUSD")).thenReturn(new BigDecimal("1.05"));

        settlementService.processDueSettlements(30L);

        assertThat(forward.getStatus()).isEqualTo(ForwardStatus.SETTLED);
        assertThat(forward.getRealizedPnl()).isEqualByComparingTo("500.00");
        assertThat(exposure.getStatus()).isEqualTo(ExposureStatus.SETTLED);
        assertThat(exposure.getUnhedgedVariance()).isEqualByComparingTo("0.00");
        verify(walletService).postSettlement(eq(user), eq(new BigDecimal("500.00")),
                eq(LedgerEntryType.FORWARD_SETTLEMENT), any());
    }

    @Test
    void anUnhedgedReceivableGainsWhenTheRateRisesAboveTheBookedRate() {
        Exposure exposure = new Exposure(user, eurUsd, ExposureDirection.RECEIVABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Unhedged invoice");

        when(exposureRepository.findByStatusNotAndDueSimDayLessThanEqual(ExposureStatus.SETTLED, 30L))
                .thenReturn(List.of(exposure));
        when(forwardContractRepository.findByExposure(exposure)).thenReturn(List.of());
        when(rateService.getCurrentRate("EURUSD")).thenReturn(new BigDecimal("1.10"));

        settlementService.processDueSettlements(30L);

        assertThat(exposure.getUnhedgedVariance()).isEqualByComparingTo("200.00");
        verifyNoInteractions(walletService);
    }

    @Test
    void anUnhedgedPayableLosesWhenTheRateRisesAboveTheBookedRate() {
        Exposure exposure = new Exposure(user, eurUsd, ExposureDirection.PAYABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Unhedged bill");

        when(exposureRepository.findByStatusNotAndDueSimDayLessThanEqual(ExposureStatus.SETTLED, 30L))
                .thenReturn(List.of(exposure));
        when(forwardContractRepository.findByExposure(exposure)).thenReturn(List.of());
        when(rateService.getCurrentRate("EURUSD")).thenReturn(new BigDecimal("1.10"));

        settlementService.processDueSettlements(30L);

        assertThat(exposure.getUnhedgedVariance()).isEqualByComparingTo("-200.00");
    }

    @Test
    void aForwardThatIsAlreadySettledIsLeftAloneRatherThanSettledTwice() {
        Exposure exposure = new Exposure(user, eurUsd, ExposureDirection.RECEIVABLE, new BigDecimal("10000"),
                new BigDecimal("1.08"), 30, "Invoice");
        exposure.applyHedge(new BigDecimal("10000"));
        ForwardContract forward = new ForwardContract(user, exposure, eurUsd, new BigDecimal("10000"),
                new BigDecimal("1.10"), ForwardDirection.SELL, 5, 20);
        forward.settle(new BigDecimal("1.07"), new BigDecimal("300.00"));

        when(exposureRepository.findByStatusNotAndDueSimDayLessThanEqual(ExposureStatus.SETTLED, 30L))
                .thenReturn(List.of(exposure));
        when(forwardContractRepository.findByExposure(exposure)).thenReturn(List.of(forward));
        when(rateService.getCurrentRate("EURUSD")).thenReturn(new BigDecimal("1.05"));

        settlementService.processDueSettlements(30L);

        assertThat(forward.getRealizedPnl()).isEqualByComparingTo("300.00");
        verifyNoInteractions(walletService);
        assertThat(exposure.getStatus()).isEqualTo(ExposureStatus.SETTLED);
    }
}
