package jmri.jmrix.purejavacomm;

import jmri.jmrix.AbstractSerialPortController;

/**
 * Comm port identifier.
 */
public class CommPortIdentifier {

    private final String _portName;

    private CommPortIdentifier(String portName) {
        this._portName = portName;
    }

    public static CommPortIdentifier getPortIdentifier(String portName) throws NoSuchPortException {
        return new CommPortIdentifier(portName);
    }

    public SerialPort open(String appName, int timeout) throws PortInUseException {
        String systemPrefix = appName;
        return new SerialPort(AbstractSerialPortController.activatePort(
                systemPrefix, _portName, log, 1, AbstractSerialPortController.Parity.NONE));
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CommPortIdentifier.class);
}
