package com.fxhedgedesk.service.simulation;

import com.fxhedgedesk.domain.*;
import com.fxhedgedesk.repository.ExposureRepository;
import com.fxhedgedesk.repository.ForwardContractRepository;
import com.fxhedgedesk.service.FxMath;
import com.fxhedgedesk.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Settles whatever comes due on a given simulated day. A forward is a real
 * cash-settled contract, so its outcome posts to the wallet. An exposure by
 * itself is not, there's no simulated invoice actually being paid, so its
 * "unhedged variance" is reporting only: what leaving that slice unhedged
 * would have cost or earned against the rate it was booked at.
 */
@Service
public class SettlementService {

    private final ExposureRepository exposureRepository;
    private final ForwardContractRepository forwardContractRepository;
    private final FxRateSimulationService rateService;
    private final WalletService walletService;

    public SettlementService(ExposureRepository exposureRepository,
                              ForwardContractRepository forwardContractRepository,
                              FxRateSimulationService rateService,
                              WalletService walletService) {
        this.exposureRepository = exposureRepository;
        this.forwardContractRepository = forwardContractRepository;
        this.rateService = rateService;
        this.walletService = walletService;
    }

    @Transactional
    public void processDueSettlements(long simDay) {
        List<Exposure> due = exposureRepository.findByStatusNotAndDueSimDayLessThanEqual(ExposureStatus.SETTLED, simDay);
        for (Exposure exposure : due) {
            settleExposure(exposure, simDay);
        }
    }

    private void settleExposure(Exposure exposure, long simDay) {
        CurrencyPair pair = exposure.getPair();
        BigDecimal settlementRate = rateService.getCurrentRate(pair.getCode());

        for (ForwardContract forward : forwardContractRepository.findByExposure(exposure)) {
            if (forward.getStatus() == ForwardStatus.OPEN) {
                settleForward(forward, settlementRate);
            }
        }

        BigDecimal unhedgedNotional = exposure.remainingToHedge();
        BigDecimal variance = BigDecimal.ZERO;
        if (unhedgedNotional.signum() > 0) {
            BigDecimal budgetedUsd = FxMath.toUsd(pair, unhedgedNotional, exposure.getBookedRate());
            BigDecimal actualUsd = FxMath.toUsd(pair, unhedgedNotional, settlementRate);
            BigDecimal rawVariance = actualUsd.subtract(budgetedUsd);
            variance = exposure.getDirection() == ExposureDirection.RECEIVABLE ? rawVariance : rawVariance.negate();
        }

        exposure.settle(settlementRate, variance);
        exposureRepository.save(exposure);
    }

    private void settleForward(ForwardContract forward, BigDecimal settlementRate) {
        CurrencyPair pair = forward.getPair();
        BigDecimal contractUsd = FxMath.toUsd(pair, forward.getNotional(), forward.getContractedRate());
        BigDecimal spotUsd = FxMath.toUsd(pair, forward.getNotional(), settlementRate);
        BigDecimal pnl = forward.getDirection() == ForwardDirection.SELL
                ? contractUsd.subtract(spotUsd)
                : spotUsd.subtract(contractUsd);

        forward.settle(settlementRate, pnl);
        forwardContractRepository.save(forward);

        walletService.postSettlement(forward.getUser(), pnl, LedgerEntryType.FORWARD_SETTLEMENT,
                "Forward settlement on " + pair.getCode() + " (" + forward.getDirection() + ")");
    }
}
