package com.fxhedgedesk.service.simulation;

import java.math.BigDecimal;

/**
 * The seam between the rate engine and however updates actually reach a
 * browser. Keeping this as an interface, instead of wiring
 * {@code SimpMessagingTemplate} straight into the simulation service, means
 * a unit test can hand it a plain lambda instead of mocking a concrete
 * Spring class.
 */
public interface RateBroadcaster {
    void broadcast(String pairCode, BigDecimal rate, long simDay);
}
