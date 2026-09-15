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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ExposureService {

    private final ExposureRepository exposureRepository;
    private final CurrencyPairRepository currencyPairRepository;
    private final FxRateSimulationService rateService;
    private final SimulationClockService clockService;

    public ExposureService(ExposureRepository exposureRepository, CurrencyPairRepository currencyPairRepository,
                            FxRateSimulationService rateService, SimulationClockService clockService) {
        this.exposureRepository = exposureRepository;
        this.currencyPairRepository = currencyPairRepository;
        this.rateService = rateService;
        this.clockService = clockService;
    }

    @Transactional
    public Exposure bookExposure(AppUser user, String pairCode, ExposureDirection direction, BigDecimal amount,
                                  long daysUntilDue, String description) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Exposure amount must be positive");
        }
        if (daysUntilDue <= 0) {
            throw new IllegalArgumentException("Exposure must be due in the future");
        }
        CurrencyPair pair = currencyPairRepository.findById(pairCode)
                .orElseThrow(() -> new NotFoundException("Unknown currency pair " + pairCode));

        BigDecimal bookedRate = rateService.getCurrentRate(pairCode);
        long dueSimDay = clockService.currentSimDay() + daysUntilDue;

        Exposure exposure = new Exposure(user, pair, direction, amount, bookedRate, dueSimDay, description);
        return exposureRepository.save(exposure);
    }

    public List<Exposure> listForUser(AppUser user) {
        return exposureRepository.findByUserOrderByDueSimDayAsc(user);
    }

    public Exposure requireOwnedBy(UUID exposureId, AppUser user) {
        Exposure exposure = exposureRepository.findById(exposureId)
                .orElseThrow(() -> new NotFoundException("Exposure not found"));
        if (!exposure.getUser().getId().equals(user.getId())) {
            throw new NotFoundException("Exposure not found");
        }
        return exposure;
    }
}
