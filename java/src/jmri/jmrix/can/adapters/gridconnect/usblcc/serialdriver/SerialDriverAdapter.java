package jmri.jmrix.can.adapters.gridconnect.usblcc.serialdriver;

import jmri.jmrix.can.adapters.gridconnect.GcSerialDriverAdapter;

/**
 * Implements SerialPortAdapter for GridConnect adapters.
 * <p>
 * This connects a USB-LCC CAN adapter via a serial com port. Normally
 * controlled by the SerialDriverFrame class.
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2009
 * @author Andrew Crosland Copyright (C) 2023
 */
public class SerialDriverAdapter extends GcSerialDriverAdapter {

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
