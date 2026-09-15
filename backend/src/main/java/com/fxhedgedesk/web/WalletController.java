package com.fxhedgedesk.web;

import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.service.WalletService;
import com.fxhedgedesk.web.dto.WalletDtos.LedgerEntryResponse;
import com.fxhedgedesk.web.dto.WalletDtos.WalletResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    public WalletResponse wallet(@AuthenticationPrincipal AppUser user) {
        return WalletResponse.from(walletService.requireWallet(user));
    }

    @GetMapping("/ledger")
    public List<LedgerEntryResponse> ledger(@AuthenticationPrincipal AppUser user) {
        return walletService.history(user).stream().map(LedgerEntryResponse::from).toList();
    }
}
