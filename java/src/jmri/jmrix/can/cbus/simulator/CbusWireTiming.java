package jmri.jmrix.can.cbus.simulator;

import javax.annotation.Nonnull;

import jmri.jmrit.simulation.link.ArbitrationPolicy;
import jmri.jmrit.simulation.link.WireTiming;
import jmri.jmrix.can.CanFrame;

/**
 * CBUS on the wire: a CAN bus at 125 kbit/s, where the frame with the lowest
 * identifier wins.
 * <p>
 * A standard (11-bit identifier) CAN data frame with n data bytes is 47 + 8n
 * bits; bit stuffing adds up to about a fifth more, which is allowed for in
 * full. So a five-byte frame takes (47 + 40) &times; 1.2 bits at 8 &micro;s,
 * 0.835 ms.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
public class CbusWireTiming implements WireTiming {

    /** CBUS bit rate, bits a second. */
    static final int BITS_PER_SECOND = 125_000;

    /**
     * {@inheritDoc}
     */
    @Override
    public long nanosOnWire(@Nonnull Object message) {
        int bytes = message instanceof CanFrame ? ((CanFrame) message).getNumDataElements() : 8;
        double bits = (47 + 8.0 * bytes) * 1.2;
        return Math.round(bits * 1e9 / BITS_PER_SECOND);
    }

    /**
     * The frame's 11-bit identifier: CBUS's four priority bits, then the CAN
     * ID. Lower wins, as on the bus.
     * {@inheritDoc}
     */
    @Override
    public int priority(@Nonnull Object message) {
        return message instanceof CanFrame ? ((CanFrame) message).getHeader() & 0x7FF : 0x7FF;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public ArbitrationPolicy policy() {
        return ArbitrationPolicy.PRIORITY;
    }
}
