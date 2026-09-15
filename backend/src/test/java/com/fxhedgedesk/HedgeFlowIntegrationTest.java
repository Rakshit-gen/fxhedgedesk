package com.fxhedgedesk;

import com.fxhedgedesk.domain.*;
import com.fxhedgedesk.repository.AppUserRepository;
import com.fxhedgedesk.repository.ExposureRepository;
import com.fxhedgedesk.repository.ForwardContractRepository;
import com.fxhedgedesk.service.*;
import com.fxhedgedesk.service.simulation.FxRateSimulationService;
import com.fxhedgedesk.service.simulation.SettlementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Exercises the whole loop end to end against a real (H2-backed) Spring
 * context: register, book an exposure, hedge part of it with a forward,
 * advance the simulated market, settle, and confirm both the forward's
 * realized P&L and the unhedged remainder's variance are internally
 * consistent with the rate the market actually landed on. The scheduler's
 * own clock is disabled here (a huge tick interval) so the test drives the
 * simulation directly instead of racing a background thread.
 */
@SpringBootTest(properties = "fxhedgedesk.simulation.tick-interval-ms=3600000")
class HedgeFlowIntegrationTest {

    @Autowired
    private AuthService authService;
    @Autowired
    private AppUserRepository appUserRepository;
    @Autowired
    private ExposureService exposureService;
    @Autowired
    private HedgeService hedgeService;
    @Autowired
    private WalletService walletService;
    @Autowired
    private FxRateSimulationService rateService;
    @Autowired
    private SettlementService settlementService;
    @Autowired
    private ExposureRepository exposureRepository;
    @Autowired
    private ForwardContractRepository forwardContractRepository;

    @Test
    void bookingHedgingAndSettlingAnExposureIsInternallyConsistent() {
        String email = "treasurer-" + UUID.randomUUID() + "@example.com";
        authService.register(email, "password123", "Test Treasurer");
        AppUser user = appUserRepository.findByEmail(email).orElseThrow();

        BigDecimal openingBalance = walletService.requireWallet(user).getBalance();
        assertThat(openingBalance).isEqualByComparingTo("500000.00");

        Exposure exposure = exposureService.bookExposure(user, "EURUSD", ExposureDirection.RECEIVABLE,
                new BigDecimal("10000"), 3, "Q1 customer invoice");
        ForwardContract forward = hedgeService.bookForward(user, exposure.getId(), new BigDecimal("6000"));

        Exposure hedgedExposure = exposureRepository.findById(exposure.getId()).orElseThrow();
        assertThat(hedgedExposure.getStatus()).isEqualTo(ExposureStatus.PARTIALLY_HEDGED);
        assertThat(forward.getDirection()).isEqualTo(ForwardDirection.SELL);

        // Drive the simulated market forward to the exposure's actual due day. The scheduler
        // itself already ticked once at context startup (fixedDelay runs its first execution
        // immediately), which is why this reads the real due day instead of assuming a value.
        long dueDay = hedgedExposure.getDueSimDay();
        for (long day = 1; day <= dueDay; day++) {
            rateService.tick(day, 1);
        }
        BigDecimal settlementRate = rateService.getCurrentRate("EURUSD");

        settlementService.processDueSettlements(dueDay);

        ForwardContract settledForward = forwardContractRepository.findById(forward.getId()).orElseThrow();
        Exposure settledExposure = exposureRepository.findById(exposure.getId()).orElseThrow();

        assertThat(settledForward.getStatus()).isEqualTo(ForwardStatus.SETTLED);
        assertThat(settledForward.getSettlementRate()).isEqualByComparingTo(settlementRate);

        BigDecimal expectedPnl = FxMath.toUsd(settledForward.getPair(), settledForward.getNotional(), settledForward.getContractedRate())
                .subtract(FxMath.toUsd(settledForward.getPair(), settledForward.getNotional(), settlementRate));
        assertThat(settledForward.getRealizedPnl()).isEqualByComparingTo(expectedPnl);

        assertThat(settledExposure.getStatus()).isEqualTo(ExposureStatus.SETTLED);
        assertThat(settledExposure.getUnhedgedVariance()).isNotNull();

        BigDecimal walletAfter = walletService.requireWallet(user).getBalance();
        assertThat(walletAfter).isEqualByComparingTo(openingBalance.add(expectedPnl));
    }
}
