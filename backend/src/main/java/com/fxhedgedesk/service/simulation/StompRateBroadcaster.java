package com.fxhedgedesk.service.simulation;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class StompRateBroadcaster implements RateBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;

    public StompRateBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void broadcast(String pairCode, BigDecimal rate, long simDay) {
        messagingTemplate.convertAndSend("/topic/rates/" + pairCode,
                new FxRateSimulationService.RateUpdate(pairCode, rate, simDay));
    }
}
