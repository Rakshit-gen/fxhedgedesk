package com.fxhedgedesk.web.dto;

import com.fxhedgedesk.domain.ForwardContract;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public class HedgeDtos {

    public record BookForwardRequest(
            @NotNull UUID exposureId,
            @NotNull @DecimalMin(value = "0.01") BigDecimal notional
    ) {
    }

    public record ForwardResponse(
            UUID id,
            UUID exposureId,
            String pairCode,
            BigDecimal notional,
            BigDecimal contractedRate,
            String direction,
            long tradeSimDay,
            long settlementSimDay,
            String status,
            BigDecimal settlementRate,
            BigDecimal realizedPnl,
            BigDecimal unrealizedPnl
    ) {
        public static ForwardResponse from(ForwardContract forward, BigDecimal unrealizedPnl) {
            return new ForwardResponse(
                    forward.getId(),
                    forward.getExposure().getId(),
                    forward.getPair().getCode(),
                    forward.getNotional(),
                    forward.getContractedRate(),
                    forward.getDirection().name(),
                    forward.getTradeSimDay(),
                    forward.getSettlementSimDay(),
                    forward.getStatus().name(),
                    forward.getSettlementRate(),
                    forward.getRealizedPnl(),
                    unrealizedPnl
            );
        }
    }
}
