// InternalDriverAdapter.java
package jmri.jmrix.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides a dummy Adapter to allow the system connection memo and multiple
 * Internal managers to be handled.
 * <P>
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 * @version	$Revision$
 */
public class InternalAdapter extends jmri.jmrix.AbstractSerialPortController
        implements jmri.jmrix.PortAdapter {

    // private control members
    private boolean opened = false;

    public InternalAdapter() {
        super(new InternalSystemConnectionMemo());
        opened = true;
        this.manufacturerName = jmri.jmrix.DCCManufacturerList.NONE;
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    public String openPort(String portName, String appName) {
        return "true";
    }

    public void configure() {
        this.getSystemConnectionMemo().configureManagers();

    }

    public boolean status() {
        return opened;
    }

    /**
     * Get an array of valid baud rates.
     */
    public String[] validBaudRates() {
        log.debug("validBaudRates should not have been invoked");
        return null;
    }

    public String getCurrentBaudRate() {
        return "";
    }

    public java.io.DataInputStream getInputStream() {
        return null;
    }

    public java.io.DataOutputStream getOutputStream() {
        return null;
    }

    @Override
    public InternalSystemConnectionMemo getSystemConnectionMemo() {
        return (InternalSystemConnectionMemo) super.getSystemConnectionMemo();
    }

    public void recover() {

    }

    private final static Logger log = LoggerFactory
            .getLogger(InternalAdapter.class.getName());

}
