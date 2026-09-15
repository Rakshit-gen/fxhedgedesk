package com.fxhedgedesk.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallet")
public class Wallet {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private BigDecimal balance;

    @Version
    private Long version;

    protected Wallet() {
    }

    public Wallet(AppUser user, BigDecimal openingBalance) {
        this.id = UUID.randomUUID();
        this.user = user;
        this.currency = "USD";
        this.balance = openingBalance;
    }

    public void credit(BigDecimal amount) {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Credit amount must be non-negative");
        }
        this.balance = this.balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        if (amount.signum() < 0) {
            throw new IllegalArgumentException("Debit amount must be non-negative");
        }
        if (this.balance.compareTo(amount) < 0) {
            throw new IllegalStateException("Insufficient wallet balance");
        }
        this.balance = this.balance.subtract(amount);
    }

    public UUID getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
