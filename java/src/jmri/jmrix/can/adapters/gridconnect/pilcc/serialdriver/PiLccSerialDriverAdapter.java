package jmri.jmrix.can.adapters.gridconnect.pilcc.serialdriver;

import jmri.jmrix.can.adapters.gridconnect.GcSerialDriverAdapter;

/**
 * Implements SerialPortAdapter for PI-LCC GridConnect adapter.
 * <p>
 * This connects a Raspberry Pi PI_LCC via a serial port. Normally
 * controlled by the SerialDriverFrame class.
 *
 * @author Andrew Crosland Copyright (C) 2023
 */
public class PiLccSerialDriverAdapter extends GcSerialDriverAdapter {

    public PiLccSerialDriverAdapter() {
        super("M", purejavacomm.SerialPort.FLOWCONTROL_RTSCTS_IN + purejavacomm.SerialPort.FLOWCONTROL_RTSCTS_OUT);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{Bundle.getMessage("Baud460800")};
    }

    /**
     * And the corresponding values.
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{460800};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

}
