package com.fxhedgedesk.service.simulation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SimulationClockServiceTest {

    private FxRateSimulationService rateService;
    private SettlementService settlementService;
    private SimulationClockService clockService;

    @BeforeEach
    void setUp() {
        rateService = mock(FxRateSimulationService.class);
        settlementService = mock(SettlementService.class);
        clockService = new SimulationClockService(rateService, settlementService, 1L);
    }

    @Test
    void startsAtDayZero() {
        assertThat(clockService.currentSimDay()).isZero();
    }

    @Test
    void eachTickAdvancesTheClockByTheConfiguredDaysPerTick() {
        clockService.tick();
        clockService.tick();
        clockService.tick();

        assertThat(clockService.currentSimDay()).isEqualTo(3L);
    }

    @Test
    void eachTickMovesRatesAndSettlesForTheNewDayInOrder() {
        clockService.tick();

        verify(rateService).tick(1L, 1L);
        verify(settlementService).processDueSettlements(1L);
    }

    @Test
    void aFasterConfiguredPaceJumpsMultipleDaysPerTick() {
        SimulationClockService fastClock = new SimulationClockService(rateService, settlementService, 5L);

        fastClock.tick();

        assertThat(fastClock.currentSimDay()).isEqualTo(5L);
        verify(rateService).tick(5L, 5L);
        verify(settlementService).processDueSettlements(5L);
    }
}
