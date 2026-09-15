package com.fxhedgedesk.service.simulation;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

/**
 * The heartbeat of the whole simulation. Each tick advances the simulated
 * calendar, moves every currency pair's rate, and settles whatever exposures
 * and forwards just came due, so a 90-day hedge plays out in minutes instead
 * of an actual quarter.
 */
@Service
public class SimulationClockService {

    private final FxRateSimulationService rateService;
    private final SettlementService settlementService;
    private final long daysPerTick;

    private final AtomicLong currentSimDay = new AtomicLong(0);

    public SimulationClockService(FxRateSimulationService rateService,
                                   SettlementService settlementService,
                                   @Value("${fxhedgedesk.simulation.days-per-tick}") long daysPerTick) {
        this.rateService = rateService;
        this.settlementService = settlementService;
        this.daysPerTick = daysPerTick;
    }

    public long currentSimDay() {
        return currentSimDay.get();
    }

    @Scheduled(fixedDelayString = "${fxhedgedesk.simulation.tick-interval-ms}")
    public void tick() {
        long day = currentSimDay.addAndGet(daysPerTick);
        rateService.tick(day, daysPerTick);
        settlementService.processDueSettlements(day);
    }
}
