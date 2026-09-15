package com.fxhedgedesk.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * A cash-settled forward: no currency actually changes hands, only the
 * difference between the contracted rate and the settlement-day spot rate,
 * the same economic effect as a real forward without needing a delivery
 * mechanism this simulation has no business modeling.
 */
@Entity
@Table(name = "forward_contract")
public class ForwardContract {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "exposure_id", nullable = false)
    private Exposure exposure;

    // Eager for the same reason as Exposure.pair: a tiny reference table read
    // after the owning transaction has closed.
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "pair_code", nullable = false)
    private CurrencyPair pair;

    @Column(nullable = false)
    private BigDecimal notional;

    @Column(name = "contracted_rate", nullable = false)
    private BigDecimal contractedRate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ForwardDirection direction;

    @Column(name = "trade_sim_day", nullable = false)
    private long tradeSimDay;

    @Column(name = "settlement_sim_day", nullable = false)
    private long settlementSimDay;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ForwardStatus status;

    @Column(name = "settlement_rate")
    private BigDecimal settlementRate;

    @Column(name = "realized_pnl")
    private BigDecimal realizedPnl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "settled_at")
    private Instant settledAt;

    protected ForwardContract() {
    }

    public ForwardContract(AppUser user, Exposure exposure, CurrencyPair pair, BigDecimal notional,
                            BigDecimal contractedRate, ForwardDirection direction, long tradeSimDay,
                            long settlementSimDay) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.exposure = exposure;
        this.pair = pair;
        this.notional = notional;
        this.contractedRate = contractedRate;
        this.direction = direction;
        this.tradeSimDay = tradeSimDay;
        this.settlementSimDay = settlementSimDay;
        this.status = ForwardStatus.OPEN;
        this.createdAt = Instant.now();
    }

    public void settle(BigDecimal settlementRate, BigDecimal realizedPnl) {
        if (status != ForwardStatus.OPEN) {
            throw new IllegalStateException("Forward is not open");
        }
        this.status = ForwardStatus.SETTLED;
        this.settledAt = Instant.now();
        this.settlementRate = settlementRate;
        this.realizedPnl = realizedPnl;
    }

    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public Exposure getExposure() {
        return exposure;
    }

    public CurrencyPair getPair() {
        return pair;
    }

    public BigDecimal getNotional() {
        return notional;
    }

    public BigDecimal getContractedRate() {
        return contractedRate;
    }

    public ForwardDirection getDirection() {
        return direction;
    }

    public long getTradeSimDay() {
        return tradeSimDay;
    }

    public long getSettlementSimDay() {
        return settlementSimDay;
    }

    public ForwardStatus getStatus() {
        return status;
    }

    public BigDecimal getSettlementRate() {
        return settlementRate;
    }

    public BigDecimal getRealizedPnl() {
        return realizedPnl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getSettledAt() {
        return settledAt;
    }
}
