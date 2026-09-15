package com.fxhedgedesk.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A future foreign-currency cashflow the desk is exposed to, an invoice
 * or a bill, due on a simulated day. {@code hedgedAmount} tracks how much
 * of it has been locked in with forward contracts; the rest rides the
 * market unhedged until settlement.
 */
@Entity
@Table(name = "exposure")
public class Exposure {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pair_code", nullable = false)
    private CurrencyPair pair;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExposureDirection direction;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(name = "hedged_amount", nullable = false)
    private BigDecimal hedgedAmount;

    @Column(name = "booked_rate", nullable = false)
    private BigDecimal bookedRate;

    @Column(name = "due_sim_day", nullable = false)
    private long dueSimDay;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExposureStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Column(name = "settlement_rate")
    private BigDecimal settlementRate;

    @Column(name = "unhedged_variance")
    private BigDecimal unhedgedVariance;

    protected Exposure() {
    }

    public Exposure(AppUser user, CurrencyPair pair, ExposureDirection direction, BigDecimal amount,
                     BigDecimal bookedRate, long dueSimDay, String description) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.pair = pair;
        this.direction = direction;
        this.amount = amount;
        this.hedgedAmount = BigDecimal.ZERO;
        this.bookedRate = bookedRate;
        this.dueSimDay = dueSimDay;
        this.description = description;
        this.status = ExposureStatus.OPEN;
        this.createdAt = Instant.now();
    }

    public BigDecimal remainingToHedge() {
        return amount.subtract(hedgedAmount);
    }

    public boolean isFullyHedged() {
        return remainingToHedge().signum() <= 0;
    }

    public void applyHedge(BigDecimal notional) {
        if (status == ExposureStatus.SETTLED) {
            throw new IllegalStateException("Exposure already settled");
        }
        if (notional.signum() <= 0) {
            throw new IllegalArgumentException("Hedge notional must be positive");
        }
        if (notional.compareTo(remainingToHedge()) > 0) {
            throw new IllegalArgumentException("Hedge notional exceeds unhedged amount");
        }
        this.hedgedAmount = this.hedgedAmount.add(notional);
        this.status = isFullyHedged() ? ExposureStatus.HEDGED : ExposureStatus.PARTIALLY_HEDGED;
    }

    public void settle(BigDecimal settlementRate, BigDecimal unhedgedVariance) {
        if (status == ExposureStatus.SETTLED) {
            throw new IllegalStateException("Exposure already settled");
        }
        this.status = ExposureStatus.SETTLED;
        this.settledAt = Instant.now();
        this.settlementRate = settlementRate;
        this.unhedgedVariance = unhedgedVariance;
    }

    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public CurrencyPair getPair() {
        return pair;
    }

    public ExposureDirection getDirection() {
        return direction;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getHedgedAmount() {
        return hedgedAmount;
    }

    public BigDecimal getBookedRate() {
        return bookedRate;
    }

    public long getDueSimDay() {
        return dueSimDay;
    }

    public String getDescription() {
        return description;
    }

    public ExposureStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSettledAt() {
        return settledAt;
    }

    public BigDecimal getSettlementRate() {
        return settlementRate;
    }

    public BigDecimal getUnhedgedVariance() {
        return unhedgedVariance;
    }
}
