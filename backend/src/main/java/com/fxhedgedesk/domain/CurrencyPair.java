package com.fxhedgedesk.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * The static definition of a simulated FX pair: where its rate starts and
 * how volatile it is. {@link com.fxhedgedesk.service.simulation.FxRateSimulationService}
 * uses these to drive the random-walk engine; nothing else about the pair
 * changes at runtime.
 */
@Entity
@Table(name = "currency_pair")
public class CurrencyPair {

    @Id
    private String code;

    @Column(name = "base_ccy", nullable = false)
    private String baseCcy;

    @Column(name = "quote_ccy", nullable = false)
    private String quoteCcy;

    @Column(name = "starting_rate", nullable = false)
    private BigDecimal startingRate;

    @Column(name = "annual_volatility", nullable = false)
    private BigDecimal annualVolatility;

    @Column(name = "annual_drift", nullable = false)
    private BigDecimal annualDrift;

    protected CurrencyPair() {
    }

    public String getCode() {
        return code;
    }

    public String getBaseCcy() {
        return baseCcy;
    }

    public String getQuoteCcy() {
        return quoteCcy;
    }

    public BigDecimal getStartingRate() {
        return startingRate;
    }

    public BigDecimal getAnnualVolatility() {
        return annualVolatility;
    }

    public BigDecimal getAnnualDrift() {
        return annualDrift;
    }
}
