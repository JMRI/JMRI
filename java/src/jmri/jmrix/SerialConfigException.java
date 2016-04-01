package jmri.jmrix;

/**
 * Represents a failure during the configuration of a serial port, typically via
 * a SerialPortAdapter interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class SerialConfigException extends jmri.JmriException {

    /**
     *
     */
    private static final long serialVersionUID = -7879594498283129336L;

    public SerialConfigException(String s) {
        super(s);
    }

    public SerialConfigException() {
    }

}
