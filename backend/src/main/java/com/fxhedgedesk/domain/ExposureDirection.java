package com.fxhedgedesk.domain;

/**
 * Which way the cash moves when the exposure settles. A RECEIVABLE is
 * foreign currency coming in (a customer invoice), so the desk is exposed
 * to that currency weakening. A PAYABLE is foreign currency going out (a
 * supplier bill), so the desk is exposed to it strengthening.
 */
public enum ExposureDirection {
    RECEIVABLE,
    PAYABLE
}
