package com.fxhedgedesk.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fx_rate_tick")
public class FxRateTick {

    @Id
    private UUID id;

    @Column(name = "pair_code", nullable = false)
    private String pairCode;

    @Column(nullable = false)
    private BigDecimal rate;

    @Column(name = "sim_day", nullable = false)
    private long simDay;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected FxRateTick() {
    }

    public FxRateTick(String pairCode, BigDecimal rate, long simDay) {
        this.id = UUID.randomUUID();
        this.pairCode = pairCode;
        this.rate = rate;
        this.simDay = simDay;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getPairCode() {
        return pairCode;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public long getSimDay() {
        return simDay;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
