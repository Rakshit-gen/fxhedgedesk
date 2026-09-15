package com.fxhedgedesk.service.simulation;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class StompRateBroadcasterTest {

    @Test
    void broadcastsToThePairSpecificTopicWithTheFullRateUpdate() {
        SimpMessagingTemplate messagingTemplate = mock(SimpMessagingTemplate.class);
        StompRateBroadcaster broadcaster = new StompRateBroadcaster(messagingTemplate);

        broadcaster.broadcast("EURUSD", new BigDecimal("1.0950"), 12L);

        verify(messagingTemplate).convertAndSend(eq("/topic/rates/EURUSD"),
                eq(new FxRateSimulationService.RateUpdate("EURUSD", new BigDecimal("1.0950"), 12L)));
    }
}
