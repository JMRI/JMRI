package jmri.jmrix.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a dummy Adapter to allow the system connection memo and multiple
 * Internal managers to be handled.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
public class InternalAdapter extends jmri.jmrix.AbstractSerialPortController
        implements jmri.jmrix.PortAdapter {

    // private control members
    private boolean opened = false;

    public InternalAdapter() {
        super(new InternalSystemConnectionMemo());
        opened = true;
        this.manufacturerName = InternalConnectionTypeList.NONE;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public String openPort(String portName, String appName) {
        return "true";
    }

    @Override
    public void configure() {
        this.getSystemConnectionMemo().configureManagers();

    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Get an array of valid baud rates.
     */
    @Override
    public String[] validBaudRates() {
        log.debug("validBaudRates should not have been invoked");
        return null;
    }

    @Override
    public String getCurrentBaudRate() {
        return "";
    }

    @Override
    public java.io.DataInputStream getInputStream() {
        return null;
    }

    @Override
    public java.io.DataOutputStream getOutputStream() {
        return null;
    }

    @Override
    public InternalSystemConnectionMemo getSystemConnectionMemo() {
        return (InternalSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    @Override
    public void recover() {

    }

    private final static Logger log = LoggerFactory
            .getLogger(InternalAdapter.class);

}
