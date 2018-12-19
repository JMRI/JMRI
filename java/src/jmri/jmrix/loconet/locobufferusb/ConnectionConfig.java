package jmri.jmrix.loconet.locobufferusb;

import jmri.util.SystemType;

/**
 * Definition of objects to handle configuring an LocoBuffer-Usb layout
 * connection via a LocoBufferIIAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
  */
public class ConnectionConfig extends jmri.jmrix.AbstractSerialConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    /**
     * Ctor for a functional Swing object with no preexisting adapter
     */
    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "LocoNet LocoBuffer-USB"; // NOI18N
    }

    public boolean isOptList2Advanced() {
        return false;
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new LocoBufferUsbAdapter();
        }
    }

    @Override
    protected String[] getPortFriendlyNames() {
        if (SystemType.isWindows()) {
            return new String[]{"LocoBuffer-USB", "LocoBuffer"}; // NOI18N
        }
        return new String[]{};
    }
}
