package com.fxhedgedesk.repository;

import com.fxhedgedesk.domain.LedgerEntry;
import com.fxhedgedesk.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByWalletOrderByCreatedAtDesc(Wallet wallet);
}
