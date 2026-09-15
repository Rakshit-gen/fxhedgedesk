package com.fxhedgedesk.web;

import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.domain.LedgerEntry;
import com.fxhedgedesk.domain.LedgerEntryType;
import com.fxhedgedesk.domain.Wallet;
import com.fxhedgedesk.exception.NotFoundException;
import com.fxhedgedesk.repository.AppUserRepository;
import com.fxhedgedesk.security.JwtService;
import com.fxhedgedesk.service.WalletService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = WalletController.class)
@AutoConfigureMockMvc(addFilters = false)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private AppUserRepository appUserRepository;

    private final AppUser user = new AppUser("desk@example.com", "hash", "Desk");

    private RequestPostProcessor asUser() {
        return SecurityMockMvcRequestPostProcessors.authentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));
    }

    @Test
    void walletReturnsTheBalanceAndCurrency() throws Exception {
        Wallet wallet = new Wallet(user, new BigDecimal("500000.00"));
        when(walletService.requireWallet(any())).thenReturn(wallet);

        mockMvc.perform(get("/api/wallet").with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(500000.00))
                .andExpect(jsonPath("$.currency").value("USD"));
    }

    @Test
    void walletForAUserWithNoWalletYetReturns404() throws Exception {
        when(walletService.requireWallet(any())).thenThrow(new NotFoundException("Wallet not found for user"));

        mockMvc.perform(get("/api/wallet").with(asUser()))
                .andExpect(status().isNotFound());
    }

    @Test
    void ledgerReturnsEntriesNewestFirstAsGivenByTheService() throws Exception {
        Wallet wallet = new Wallet(user, new BigDecimal("500000.00"));
        LedgerEntry entry = new LedgerEntry(wallet, LedgerEntryType.FORWARD_SETTLEMENT, new BigDecimal("250.00"), "Forward gain");
        when(walletService.history(any())).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/wallet/ledger").with(asUser()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].entryType").value("FORWARD_SETTLEMENT"))
                .andExpect(jsonPath("$[0].amount").value(250.00));
    }
}
