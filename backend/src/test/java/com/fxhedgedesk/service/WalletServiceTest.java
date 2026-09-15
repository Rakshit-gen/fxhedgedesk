package com.fxhedgedesk.service;

import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.domain.LedgerEntryType;
import com.fxhedgedesk.domain.Wallet;
import com.fxhedgedesk.exception.NotFoundException;
import com.fxhedgedesk.repository.LedgerEntryRepository;
import com.fxhedgedesk.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    private final AppUser user = new AppUser("desk@example.com", "hash", "Desk");

    private WalletRepository walletRepository;
    private LedgerEntryRepository ledgerEntryRepository;
    private WalletService walletService;

    @BeforeEach
    void setUp() {
        walletRepository = mock(WalletRepository.class);
        ledgerEntryRepository = mock(LedgerEntryRepository.class);
        walletService = new WalletService(walletRepository, ledgerEntryRepository);

        when(walletRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void requireWalletFailsLoudlyWhenTheUserHasNone() {
        when(walletRepository.findByUser(user)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.requireWallet(user)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void aPositiveSettlementCreditsTheWalletAndLogsAGain() {
        Wallet wallet = new Wallet(user, new BigDecimal("500000.00"));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        walletService.postSettlement(user, new BigDecimal("250.00"), LedgerEntryType.FORWARD_SETTLEMENT, "Forward gain");

        assertThat(wallet.getBalance()).isEqualByComparingTo("500250.00");
        verify(ledgerEntryRepository).save(argThat(entry -> entry.getAmount().compareTo(new BigDecimal("250.00")) == 0));
    }

    @Test
    void aNegativeSettlementDebitsTheWalletByItsAbsoluteValue() {
        Wallet wallet = new Wallet(user, new BigDecimal("500000.00"));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        walletService.postSettlement(user, new BigDecimal("-99.00"), LedgerEntryType.FORWARD_SETTLEMENT, "Forward loss");

        assertThat(wallet.getBalance()).isEqualByComparingTo("499901.00");
    }

    @Test
    void aLossBiggerThanTheBalanceIsRejectedRatherThanGoingNegative() {
        Wallet wallet = new Wallet(user, new BigDecimal("100.00"));
        when(walletRepository.findByUser(user)).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> walletService.postSettlement(user, new BigDecimal("-500.00"),
                LedgerEntryType.FORWARD_SETTLEMENT, "Bigger than balance"))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(ledgerEntryRepository);
    }
}
