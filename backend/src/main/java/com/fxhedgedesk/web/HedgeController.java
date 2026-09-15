package com.fxhedgedesk.web;

import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.domain.ForwardContract;
import com.fxhedgedesk.service.HedgeService;
import com.fxhedgedesk.web.dto.HedgeDtos.BookForwardRequest;
import com.fxhedgedesk.web.dto.HedgeDtos.ForwardResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forwards")
public class HedgeController {

    private final HedgeService hedgeService;

    public HedgeController(HedgeService hedgeService) {
        this.hedgeService = hedgeService;
    }

    @PostMapping
    public ForwardResponse book(@AuthenticationPrincipal AppUser user, @Valid @RequestBody BookForwardRequest request) {
        ForwardContract forward = hedgeService.bookForward(user, request.exposureId(), request.notional());
        return ForwardResponse.from(forward, hedgeService.unrealizedPnl(forward));
    }

    @GetMapping
    public List<ForwardResponse> list(@AuthenticationPrincipal AppUser user) {
        return hedgeService.listForUser(user).stream()
                .map(forward -> ForwardResponse.from(forward, hedgeService.unrealizedPnl(forward)))
                .toList();
    }
}
