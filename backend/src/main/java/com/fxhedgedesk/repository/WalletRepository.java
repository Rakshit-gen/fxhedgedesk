package com.fxhedgedesk.repository;

import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.domain.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    Optional<Wallet> findByUser(AppUser user);
}
