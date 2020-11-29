package jmri.jmrix.nce.usbdriver;

import jmri.util.SystemType;

/**
 * Definition of objects to handle configuring an LocoBuffer layout connection
 * via an NCE SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 * @author Daniel Boudreau Copyright (C) 2007
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    public final static String NAME = "NCE USB"; // NOI18N

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p SerialPortAdapter to configure
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no existing adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return NAME;
    }

    public boolean isOptList1Advanced() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new UsbDriverAdapter();
        }
    }

    @Override
    protected String[] getPortFriendlyNames() {
        if (SystemType.isWindows()) {
            return new String[]{"Silicon Labs CP210x USB to UART Bridge", "Silicon Labs CP210x"}; // NOI18N
        }
        return new String[]{};
    }

}
