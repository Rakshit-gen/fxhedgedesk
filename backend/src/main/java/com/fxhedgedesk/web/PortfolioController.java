package com.fxhedgedesk.web;

import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.service.PortfolioService;
import com.fxhedgedesk.service.PortfolioSummary;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    public PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @GetMapping
    public PortfolioSummary summary(@AuthenticationPrincipal AppUser user) {
        return portfolioService.summarize(user);
    }
}
