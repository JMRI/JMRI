package jmri.jmrix.purejavacomm;

import jmri.SystemConnectionMemo;
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

    public SerialPort open(SystemConnectionMemo memo, int timeout) throws PortInUseException, NoSuchPortException {
        jmri.jmrix.SerialPort serialPort = AbstractSerialPortController.activatePort(
                memo, _portName, log, 1, jmri.jmrix.SerialPort.Parity.NONE);

        if (serialPort != null) {
            return new SerialPort(serialPort);
        } else {
            throw new NoSuchPortException();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CommPortIdentifier.class);
}
