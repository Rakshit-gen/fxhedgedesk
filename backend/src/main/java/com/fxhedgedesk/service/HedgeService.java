package com.fxhedgedesk.service;

import com.fxhedgedesk.domain.*;
import com.fxhedgedesk.repository.ExposureRepository;
import com.fxhedgedesk.repository.ForwardContractRepository;
import com.fxhedgedesk.service.simulation.FxRateSimulationService;
import com.fxhedgedesk.service.simulation.SimulationClockService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class HedgeService {

    private final ExposureService exposureService;
    private final ExposureRepository exposureRepository;
    private final ForwardContractRepository forwardContractRepository;
    private final FxRateSimulationService rateService;
    private final SimulationClockService clockService;

    public HedgeService(ExposureService exposureService, ExposureRepository exposureRepository,
                         ForwardContractRepository forwardContractRepository, FxRateSimulationService rateService,
                         SimulationClockService clockService) {
        this.exposureService = exposureService;
        this.exposureRepository = exposureRepository;
        this.forwardContractRepository = forwardContractRepository;
        this.rateService = rateService;
        this.clockService = clockService;
    }

    @Transactional
    public ForwardContract bookForward(AppUser user, UUID exposureId, BigDecimal notional) {
        Exposure exposure = exposureService.requireOwnedBy(exposureId, user);
        CurrencyPair pair = exposure.getPair();
        ForwardDirection direction = exposure.getDirection() == ExposureDirection.RECEIVABLE
                ? ForwardDirection.SELL
                : ForwardDirection.BUY;

        BigDecimal contractedRate = rateService.getCurrentRate(pair.getCode());
        long tradeSimDay = clockService.currentSimDay();

        exposure.applyHedge(notional);
        exposureRepository.save(exposure);

        ForwardContract forward = new ForwardContract(user, exposure, pair, notional, contractedRate, direction,
                tradeSimDay, exposure.getDueSimDay());
        return forwardContractRepository.save(forward);
    }

    public List<ForwardContract> listForUser(AppUser user) {
        return forwardContractRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /** Unrealized P&L an open forward would post if it settled at today's rate. */
    public BigDecimal unrealizedPnl(ForwardContract forward) {
        if (forward.getStatus() != ForwardStatus.OPEN) {
            return forward.getRealizedPnl();
        }
        CurrencyPair pair = forward.getPair();
        BigDecimal currentRate = rateService.getCurrentRate(pair.getCode());
        BigDecimal contractUsd = FxMath.toUsd(pair, forward.getNotional(), forward.getContractedRate());
        BigDecimal spotUsd = FxMath.toUsd(pair, forward.getNotional(), currentRate);
        return forward.getDirection() == ForwardDirection.SELL
                ? contractUsd.subtract(spotUsd)
                : spotUsd.subtract(contractUsd);
    }
}
