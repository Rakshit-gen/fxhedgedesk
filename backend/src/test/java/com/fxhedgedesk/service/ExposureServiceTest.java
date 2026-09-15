package com.fxhedgedesk.service;

import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.domain.CurrencyPair;
import com.fxhedgedesk.domain.Exposure;
import com.fxhedgedesk.domain.ExposureDirection;
import com.fxhedgedesk.exception.NotFoundException;
import com.fxhedgedesk.repository.CurrencyPairRepository;
import com.fxhedgedesk.repository.ExposureRepository;
import com.fxhedgedesk.service.simulation.FxRateSimulationService;
import com.fxhedgedesk.service.simulation.SimulationClockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExposureServiceTest {

    private ExposureRepository exposureRepository;
    private CurrencyPairRepository currencyPairRepository;
    private FxRateSimulationService rateService;
    private SimulationClockService clockService;
    private ExposureService exposureService;

    private final AppUser user = new AppUser("desk@example.com", "hash", "Desk");
    private final CurrencyPair eurUsd = new CurrencyPair("EURUSD", "EUR", "USD", new BigDecimal("1.08"),
            new BigDecimal("0.08"), BigDecimal.ZERO);

    @BeforeEach
    void setUp() {
        exposureRepository = mock(ExposureRepository.class);
        currencyPairRepository = mock(CurrencyPairRepository.class);
        rateService = mock(FxRateSimulationService.class);
        clockService = mock(SimulationClockService.class);
        exposureService = new ExposureService(exposureRepository, currencyPairRepository, rateService, clockService);

        when(exposureRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void bookingAnExposureUsesTodaysSimulatedRateAndClockDay() {
        when(currencyPairRepository.findById("EURUSD")).thenReturn(Optional.of(eurUsd));
        when(rateService.getCurrentRate("EURUSD")).thenReturn(new BigDecimal("1.0950"));
        when(clockService.currentSimDay()).thenReturn(40L);

        Exposure exposure = exposureService.bookExposure(user, "EURUSD", ExposureDirection.RECEIVABLE,
                new BigDecimal("8000"), 15, "Test invoice");

        assertThat(exposure.getBookedRate()).isEqualByComparingTo("1.0950");
        assertThat(exposure.getDueSimDay()).isEqualTo(55L);
    }

    @Test
    void bookingAgainstAnUnknownPairIsRejected() {
        when(currencyPairRepository.findById("ZZZXXX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> exposureService.bookExposure(user, "ZZZXXX", ExposureDirection.PAYABLE,
                BigDecimal.TEN, 10, "Bad pair"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void bookingWithANonPositiveAmountIsRejected() {
        assertThatThrownBy(() -> exposureService.bookExposure(user, "EURUSD", ExposureDirection.RECEIVABLE,
                BigDecimal.ZERO, 10, "Zero amount"))
                .isInstanceOf(IllegalArgumentException.class);
        verifyNoInteractions(currencyPairRepository);
    }

    @Test
    void bookingWithoutAFutureDueDayIsRejected() {
        assertThatThrownBy(() -> exposureService.bookExposure(user, "EURUSD", ExposureDirection.RECEIVABLE,
                BigDecimal.TEN, 0, "Due today"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void fetchingAnExposureOwnedBySomeoneElseLooksLikeItDoesNotExist() {
        AppUser owner = new AppUser("owner@example.com", "hash", "Owner");
        AppUser stranger = new AppUser("stranger@example.com", "hash", "Stranger");
        Exposure exposure = new Exposure(owner, eurUsd, ExposureDirection.RECEIVABLE, BigDecimal.TEN,
                new BigDecimal("1.08"), 10, "Owner's exposure");
        UUID exposureId = exposure.getId();
        when(exposureRepository.findById(exposureId)).thenReturn(Optional.of(exposure));

        assertThatThrownBy(() -> exposureService.requireOwnedBy(exposureId, stranger))
                .isInstanceOf(NotFoundException.class);
    }
}
