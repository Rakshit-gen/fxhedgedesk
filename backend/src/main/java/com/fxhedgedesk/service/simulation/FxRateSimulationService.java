package com.fxhedgedesk.service.simulation;

import com.fxhedgedesk.domain.CurrencyPair;
import com.fxhedgedesk.domain.FxRateTick;
import com.fxhedgedesk.repository.CurrencyPairRepository;
import com.fxhedgedesk.repository.FxRateTickRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Drives a geometric Brownian motion for each currency pair: the same model
 * used to sanity-check option pricing, just run forward instead of solved
 * backward. Rates live in memory between ticks so reads never hit the
 * database; each tick is additionally persisted so the frontend has a
 * history to chart.
 */
@Service
public class FxRateSimulationService {

    private static final MathContext MC = new MathContext(12);

    private final CurrencyPairRepository currencyPairRepository;
    private final FxRateTickRepository fxRateTickRepository;
    private final RateBroadcaster broadcaster;
    private final Random random;

    private final Map<String, BigDecimal> currentRates = new ConcurrentHashMap<>();
    private volatile boolean seeded = false;

    @Autowired
    public FxRateSimulationService(CurrencyPairRepository currencyPairRepository,
                                    FxRateTickRepository fxRateTickRepository,
                                    RateBroadcaster broadcaster) {
        this(currencyPairRepository, fxRateTickRepository, broadcaster, new Random());
    }

    public FxRateSimulationService(CurrencyPairRepository currencyPairRepository,
                                    FxRateTickRepository fxRateTickRepository,
                                    RateBroadcaster broadcaster,
                                    Random random) {
        this.currencyPairRepository = currencyPairRepository;
        this.fxRateTickRepository = fxRateTickRepository;
        this.broadcaster = broadcaster;
        this.random = random;
    }

    private void seedIfNeeded() {
        if (seeded) {
            return;
        }
        synchronized (this) {
            if (seeded) {
                return;
            }
            for (CurrencyPair pair : currencyPairRepository.findAll()) {
                currentRates.put(pair.getCode(), pair.getStartingRate());
            }
            seeded = true;
        }
    }

    public BigDecimal getCurrentRate(String pairCode) {
        seedIfNeeded();
        BigDecimal rate = currentRates.get(pairCode);
        if (rate == null) {
            throw new IllegalArgumentException("Unknown currency pair " + pairCode);
        }
        return rate;
    }

    @Transactional
    public void tick(long simDay, long daysPerTick) {
        seedIfNeeded();
        double dt = daysPerTick / 365.0;

        for (CurrencyPair pair : currencyPairRepository.findAll()) {
            BigDecimal previous = currentRates.get(pair.getCode());
            BigDecimal next = step(previous, pair.getAnnualDrift(), pair.getAnnualVolatility(), dt);
            currentRates.put(pair.getCode(), next);

            fxRateTickRepository.save(new FxRateTick(pair.getCode(), next, simDay));
            broadcaster.broadcast(pair.getCode(), next, simDay);
        }
    }

    private BigDecimal step(BigDecimal previous, BigDecimal annualDrift, BigDecimal annualVolatility, double dt) {
        double s = previous.doubleValue();
        double mu = annualDrift.doubleValue();
        double sigma = annualVolatility.doubleValue();
        double z = random.nextGaussian();

        double drift = (mu - 0.5 * sigma * sigma) * dt;
        double diffusion = sigma * Math.sqrt(dt) * z;
        double next = s * Math.exp(drift + diffusion);

        return BigDecimal.valueOf(next).round(MC).setScale(6, RoundingMode.HALF_UP);
    }

    public List<CurrencyPair> pairs() {
        return currencyPairRepository.findAll();
    }

    public record RateUpdate(String pairCode, BigDecimal rate, long simDay) {
    }
}
