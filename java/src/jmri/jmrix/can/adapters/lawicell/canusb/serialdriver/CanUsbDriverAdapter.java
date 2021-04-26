package jmri.jmrix.can.adapters.lawicell.canusb.serialdriver;

/**
 * Implements SerialPortAdapter for the CAN-USB.
 * <p>
 * This connects a CAN-USB CAN adapter via a serial com port. Normally
 * controlled by the SerialDriverFrame class.
 *
 * @author Andrew Crosland Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2008, 2010
 */
public class CanUsbDriverAdapter extends jmri.jmrix.can.adapters.lawicell.SerialDriverAdapter {

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{Bundle.getMessage("Baud57600"),
                Bundle.getMessage("Baud115200"), Bundle.getMessage("Baud230400"),
                Bundle.getMessage("Baud250000"), Bundle.getMessage("Baud333333"),
                Bundle.getMessage("Baud460800"), Bundle.getMessage("Baud500000")};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{57600, 115200, 230400, 250000, 333333, 460800, 500000};
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
