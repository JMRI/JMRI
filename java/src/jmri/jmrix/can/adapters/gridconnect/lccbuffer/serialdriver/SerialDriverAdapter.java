package jmri.jmrix.can.adapters.gridconnect.lccbuffer.serialdriver;

import jmri.jmrix.can.adapters.gridconnect.GcSerialDriverAdapter;

/**
 * Implements SerialPortAdapter for GridConnect adapters.
 * <p>
 * This connects a RR-Cirkits LCCBuffer CAN adapter via a serial com 
 * port. Normally controlled by the SerialDriverFrame class.
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class SerialDriverAdapter extends GcSerialDriverAdapter {

    /**
     * {@inheritDoc}
     *
     * TODO I18N using Bundle
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"57,600", "115,200", "230,400", "250,000", "288,000", "333,333", "460,800"};
    }

    /**
     * And the corresponding values.
     */
    @Override
    public int[] validBaudValues() {
        return new int[]{57600, 115200, 230400, 250000, 288000, 333333, 460800};
    }

    /**
     * {@inheritDoc}
     *
     * Migration method
     */
    @Override
    public int[] validBaudNumbers() {
        return validBaudValues();
    }

}
