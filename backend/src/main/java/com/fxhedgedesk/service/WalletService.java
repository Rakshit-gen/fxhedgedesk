package com.fxhedgedesk.service;

import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.domain.LedgerEntry;
import com.fxhedgedesk.domain.LedgerEntryType;
import com.fxhedgedesk.domain.Wallet;
import com.fxhedgedesk.exception.NotFoundException;
import com.fxhedgedesk.repository.LedgerEntryRepository;
import com.fxhedgedesk.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WalletService(WalletRepository walletRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.walletRepository = walletRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public Wallet openWallet(AppUser user, BigDecimal openingBalance) {
        Wallet wallet = new Wallet(user, openingBalance);
        return walletRepository.save(wallet);
    }

    public Wallet requireWallet(AppUser user) {
        return walletRepository.findByUser(user)
                .orElseThrow(() -> new NotFoundException("Wallet not found for user"));
    }

    /**
     * Posts a signed cash movement: positive credits the wallet, negative debits it.
     * ponytail: a loss larger than the wallet balance throws rather than drawing on
     * a credit line or margin account. Add one if desks ever need to run negative.
     */
    @Transactional
    public void postSettlement(AppUser user, BigDecimal signedAmount, LedgerEntryType type, String description) {
        Wallet wallet = requireWallet(user);
        if (signedAmount.signum() >= 0) {
            wallet.credit(signedAmount);
        } else {
            wallet.debit(signedAmount.abs());
        }
        walletRepository.save(wallet);
        ledgerEntryRepository.save(new LedgerEntry(wallet, type, signedAmount, description));
    }

    public List<LedgerEntry> history(AppUser user) {
        return ledgerEntryRepository.findByWalletOrderByCreatedAtDesc(requireWallet(user));
    }
}
