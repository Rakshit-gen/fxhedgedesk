package com.fxhedgedesk.service;

import com.fxhedgedesk.domain.*;
import com.fxhedgedesk.repository.ExposureRepository;
import com.fxhedgedesk.repository.ForwardContractRepository;
import com.fxhedgedesk.service.simulation.FxRateSimulationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

/**
 * The numbers a treasury desk actually watches: how much foreign-currency
 * risk is on the books, how much of it is locked in, and a rough estimate
 * of what a single bad day could cost. The VaR here is a standard parametric
 * (variance-covariance) estimate, not a full historical simulation, and it
 * sums per-currency VaR without modeling cross-currency correlation.
 * ponytail: correlated pairs (EURUSD and GBPUSD both moving with the dollar)
 * mean this overstates diversifiable risk; a covariance matrix fixes it if
 * the desk ever holds enough pairs for correlation to matter.
 */
@Service
public class PortfolioService {

    private static final BigDecimal Z_95 = new BigDecimal("1.645");
    private static final MathContext MC = new MathContext(12);

    private final ExposureRepository exposureRepository;
    private final ForwardContractRepository forwardContractRepository;
    private final FxRateSimulationService rateService;
    private final HedgeService hedgeService;

    public PortfolioService(ExposureRepository exposureRepository, ForwardContractRepository forwardContractRepository,
                             FxRateSimulationService rateService, HedgeService hedgeService) {
        this.exposureRepository = exposureRepository;
        this.forwardContractRepository = forwardContractRepository;
        this.rateService = rateService;
        this.hedgeService = hedgeService;
    }

    @Transactional(readOnly = true)
    public PortfolioSummary summarize(AppUser user) {
        List<Exposure> exposures = exposureRepository.findByUserOrderByDueSimDayAsc(user);
        List<Exposure> openExposures = exposures.stream()
                .filter(e -> e.getStatus() != ExposureStatus.SETTLED)
                .toList();

        BigDecimal totalUsd = BigDecimal.ZERO;
        BigDecimal totalHedgedUsd = BigDecimal.ZERO;
        BigDecimal totalVar = BigDecimal.ZERO;
        List<PortfolioSummary.CurrencyBreakdown> breakdown = new ArrayList<>();

        Map<String, List<Exposure>> grouped = new LinkedHashMap<>();
        for (Exposure exposure : openExposures) {
            grouped.computeIfAbsent(exposure.getPair().getCode(), k -> new ArrayList<>()).add(exposure);
        }

        for (Map.Entry<String, List<Exposure>> entry : grouped.entrySet()) {
            CurrencyPair pair = entry.getValue().get(0).getPair();
            BigDecimal currentRate = rateService.getCurrentRate(pair.getCode());

            BigDecimal pairTotalUsd = BigDecimal.ZERO;
            BigDecimal pairHedgedUsd = BigDecimal.ZERO;
            for (Exposure exposure : entry.getValue()) {
                pairTotalUsd = pairTotalUsd.add(FxMath.toUsd(pair, exposure.getAmount(), currentRate));
                pairHedgedUsd = pairHedgedUsd.add(FxMath.toUsd(pair, exposure.getHedgedAmount(), currentRate));
            }
            BigDecimal pairUnhedgedUsd = pairTotalUsd.subtract(pairHedgedUsd);
            BigDecimal pairHedgeRatio = pairTotalUsd.signum() == 0
                    ? BigDecimal.ZERO
                    : pairHedgedUsd.divide(pairTotalUsd, MC).setScale(4, RoundingMode.HALF_UP);

            double dailyVol = pair.getAnnualVolatility().doubleValue() / Math.sqrt(365.0);
            BigDecimal pairVar = pairUnhedgedUsd.multiply(BigDecimal.valueOf(dailyVol)).multiply(Z_95)
                    .setScale(2, RoundingMode.HALF_UP).abs();

            breakdown.add(new PortfolioSummary.CurrencyBreakdown(
                    pair.getCode(), pairTotalUsd.setScale(2, RoundingMode.HALF_UP),
                    pairHedgedUsd.setScale(2, RoundingMode.HALF_UP),
                    pairUnhedgedUsd.setScale(2, RoundingMode.HALF_UP),
                    pairHedgeRatio, pairVar));

            totalUsd = totalUsd.add(pairTotalUsd);
            totalHedgedUsd = totalHedgedUsd.add(pairHedgedUsd);
            totalVar = totalVar.add(pairVar);
        }

        BigDecimal hedgeRatio = totalUsd.signum() == 0
                ? BigDecimal.ZERO
                : totalHedgedUsd.divide(totalUsd, MC).setScale(4, RoundingMode.HALF_UP);

        List<ForwardContract> forwards = forwardContractRepository.findByUserOrderByCreatedAtDesc(user);
        BigDecimal openMtm = BigDecimal.ZERO;
        BigDecimal realizedPnl = BigDecimal.ZERO;
        for (ForwardContract forward : forwards) {
            if (forward.getStatus() == ForwardStatus.OPEN) {
                openMtm = openMtm.add(hedgeService.unrealizedPnl(forward));
            } else if (forward.getStatus() == ForwardStatus.SETTLED) {
                realizedPnl = realizedPnl.add(forward.getRealizedPnl());
            }
        }

        return new PortfolioSummary(
                totalUsd.setScale(2, RoundingMode.HALF_UP),
                totalHedgedUsd.setScale(2, RoundingMode.HALF_UP),
                hedgeRatio,
                totalVar.setScale(2, RoundingMode.HALF_UP),
                openMtm.setScale(2, RoundingMode.HALF_UP),
                realizedPnl.setScale(2, RoundingMode.HALF_UP),
                breakdown
        );
    }
}
