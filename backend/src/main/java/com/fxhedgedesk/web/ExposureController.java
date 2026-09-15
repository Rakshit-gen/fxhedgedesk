package com.fxhedgedesk.web;

import com.fxhedgedesk.domain.AppUser;
import com.fxhedgedesk.domain.Exposure;
import com.fxhedgedesk.service.ExposureService;
import com.fxhedgedesk.web.dto.ExposureDtos.BookExposureRequest;
import com.fxhedgedesk.web.dto.ExposureDtos.ExposureResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exposures")
public class ExposureController {

    private final ExposureService exposureService;

    public ExposureController(ExposureService exposureService) {
        this.exposureService = exposureService;
    }

    @PostMapping
    public ExposureResponse book(@AuthenticationPrincipal AppUser user, @Valid @RequestBody BookExposureRequest request) {
        Exposure exposure = exposureService.bookExposure(user, request.pairCode(), request.direction(),
                request.amount(), request.daysUntilDue(), request.description());
        return ExposureResponse.from(exposure);
    }

    @GetMapping
    public List<ExposureResponse> list(@AuthenticationPrincipal AppUser user) {
        return exposureService.listForUser(user).stream().map(ExposureResponse::from).toList();
    }
}
