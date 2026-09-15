package com.fxhedgedesk.service.simulation;

import com.fxhedgedesk.domain.CurrencyPair;
import com.fxhedgedesk.repository.CurrencyPairRepository;
import com.fxhedgedesk.repository.FxRateTickRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FxRateSimulationServiceTest {

    private CurrencyPairRepository currencyPairRepository;
    private FxRateTickRepository fxRateTickRepository;
    private CurrencyPair eurUsd;

    @BeforeEach
    void setUp() {
        currencyPairRepository = mock(CurrencyPairRepository.class);
        fxRateTickRepository = mock(FxRateTickRepository.class);
        eurUsd = new CurrencyPair("EURUSD", "EUR", "USD", new BigDecimal("1.0850"),
                new BigDecimal("0.08"), BigDecimal.ZERO);
        when(currencyPairRepository.findAll()).thenReturn(List.of(eurUsd));
    }

    @Test
    void startsFromThePairsStartingRate() {
        FxRateSimulationService service = new FxRateSimulationService(
                currencyPairRepository, fxRateTickRepository, (code, rate, day) -> { }, new Random(42));

        assertThat(service.getCurrentRate("EURUSD")).isEqualByComparingTo("1.0850");
    }

    @Test
    void tickMovesTheRateAndBroadcastsIt() {
        List<BigDecimal> broadcastRates = new ArrayList<>();
        RateBroadcaster broadcaster = (code, rate, day) -> broadcastRates.add(rate);

        FxRateSimulationService service = new FxRateSimulationService(
                currencyPairRepository, fxRateTickRepository, broadcaster, new Random(42));

        service.tick(1, 1);

        BigDecimal newRate = service.getCurrentRate("EURUSD");
        assertThat(newRate).isNotEqualByComparingTo("1.0850");
        assertThat(newRate.signum()).isPositive();
        assertThat(broadcastRates).containsExactly(newRate);

        verify(fxRateTickRepository, times(1)).save(any());
    }

    @Test
    void sameSeedProducesTheSameWalkDeterministically() {
        FxRateSimulationService serviceA = new FxRateSimulationService(
                currencyPairRepository, fxRateTickRepository, (code, rate, day) -> { }, new Random(7));
        FxRateSimulationService serviceB = new FxRateSimulationService(
                currencyPairRepository, fxRateTickRepository, (code, rate, day) -> { }, new Random(7));

        serviceA.tick(1, 1);
        serviceB.tick(1, 1);

        assertThat(serviceA.getCurrentRate("EURUSD")).isEqualByComparingTo(serviceB.getCurrentRate("EURUSD"));
    }

    @Test
    void askingForARateOnAnUnknownPairFailsLoudlyInsteadOfReturningNull() {
        FxRateSimulationService service = new FxRateSimulationService(
                currencyPairRepository, fxRateTickRepository, (code, rate, day) -> { }, new Random(42));

        assertThatThrownBy(() -> service.getCurrentRate("ZZZXXX")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void pairsReturnsWhateverTheRepositorySeedsIt() {
        CurrencyPair usdJpy = new CurrencyPair("USDJPY", "USD", "JPY", new BigDecimal("150"),
                new BigDecimal("0.09"), BigDecimal.ZERO);
        when(currencyPairRepository.findAll()).thenReturn(List.of(eurUsd, usdJpy));
        FxRateSimulationService service = new FxRateSimulationService(
                currencyPairRepository, fxRateTickRepository, (code, rate, day) -> { }, new Random(42));

        assertThat(service.pairs()).containsExactly(eurUsd, usdJpy);
    }

    @Test
    void eachPairMovesIndependentlyOnATick() {
        CurrencyPair usdJpy = new CurrencyPair("USDJPY", "USD", "JPY", new BigDecimal("150"),
                new BigDecimal("0.09"), BigDecimal.ZERO);
        when(currencyPairRepository.findAll()).thenReturn(List.of(eurUsd, usdJpy));
        FxRateSimulationService service = new FxRateSimulationService(
                currencyPairRepository, fxRateTickRepository, (code, rate, day) -> { }, new Random(42));

        service.tick(1, 1);

        assertThat(service.getCurrentRate("EURUSD")).isNotEqualByComparingTo("1.0850");
        assertThat(service.getCurrentRate("USDJPY")).isNotEqualByComparingTo("150");
        verify(fxRateTickRepository, times(2)).save(any());
    }
}
