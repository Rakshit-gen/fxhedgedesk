package com.fxhedgedesk.web.dto;

import com.fxhedgedesk.domain.LedgerEntry;
import com.fxhedgedesk.domain.Wallet;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public class WalletDtos {

    public record WalletResponse(BigDecimal balance, String currency) {
        public static WalletResponse from(Wallet wallet) {
            return new WalletResponse(wallet.getBalance(), wallet.getCurrency());
        }
    }

    public record LedgerEntryResponse(UUID id, String entryType, BigDecimal amount, BigDecimal balanceAfter,
                                       String description, Instant createdAt) {
        public static LedgerEntryResponse from(LedgerEntry entry) {
            return new LedgerEntryResponse(entry.getId(), entry.getEntryType().name(), entry.getAmount(),
                    entry.getBalanceAfter(), entry.getDescription(), entry.getCreatedAt());
        }
    }
}
