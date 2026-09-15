package com.fxhedgedesk.domain;

/**
 * BUY locks in a rate to buy the base currency at settlement (hedges a
 * PAYABLE). SELL locks in a rate to sell the base currency at settlement
 * (hedges a RECEIVABLE).
 */
public enum ForwardDirection {
    BUY,
    SELL
}
