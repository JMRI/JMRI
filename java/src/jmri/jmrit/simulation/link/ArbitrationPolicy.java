package jmri.jmrit.simulation.link;

/**
 * How a medium picks the next message when several are waiting for the wire.
 * More are added as systems that need them join: collision and back-off for
 * LocoNet, polling for C/MRI.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public enum ArbitrationPolicy {
    /**
     * The message with the lowest priority value goes first; equal ones in
     * the order they arrived. CAN arbitration: CBUS, OpenLCB.
     */
    PRIORITY,
    /**
     * In the order they arrived: a point-to-point serial line.
     */
    FIFO
}
