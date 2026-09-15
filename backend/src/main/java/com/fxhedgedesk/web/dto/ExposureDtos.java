package com.fxhedgedesk.web.dto;

import com.fxhedgedesk.domain.Exposure;
import com.fxhedgedesk.domain.ExposureDirection;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class ExposureDtos {

    public record BookExposureRequest(
            @NotBlank String pairCode,
            @NotNull ExposureDirection direction,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @Min(1) long daysUntilDue,
            @NotBlank String description
    ) {
    }

    public record ExposureResponse(
            UUID id,
            String pairCode,
            ExposureDirection direction,
            BigDecimal amount,
            BigDecimal hedgedAmount,
            BigDecimal bookedRate,
            long dueSimDay,
            String description,
            String status,
            BigDecimal settlementRate,
            BigDecimal unhedgedVariance
    ) {
        public static ExposureResponse from(Exposure exposure) {
            return new ExposureResponse(
                    exposure.getId(),
                    exposure.getPair().getCode(),
                    exposure.getDirection(),
                    exposure.getAmount(),
                    exposure.getHedgedAmount(),
                    exposure.getBookedRate(),
                    exposure.getDueSimDay(),
                    exposure.getDescription(),
                    exposure.getStatus().name(),
                    exposure.getSettlementRate(),
                    exposure.getUnhedgedVariance()
            );
        }
    }
}
