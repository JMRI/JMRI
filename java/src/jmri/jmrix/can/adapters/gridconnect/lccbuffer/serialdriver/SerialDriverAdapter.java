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
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{Bundle.getMessage("Baud57600"),
                Bundle.getMessage("Baud115200"), Bundle.getMessage("Baud230400"),
                Bundle.getMessage("Baud250000"), Bundle.getMessage("Baud288000"),
                Bundle.getMessage("Baud333333"), Bundle.getMessage("Baud460800")};
    }

    /**
     * And the corresponding values.
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{57600, 115200, 230400, 250000, 288000, 333333, 460800};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    /**
     * {@inheritDoc}
     * Migration method
     * @deprecated since 4.16
     */
    @Deprecated
    @Override
    public int[] validBaudValues() {
        return validBaudNumbers();
    }

}
