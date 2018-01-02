package jmri.jmrix.loconet.hexfile;

import jmri.jmrix.AbstractSimulatorConnectionConfig;
import jmri.jmrix.SerialPortAdapter;

/**
 * Definition of objects to handle configuring a layout connection via a LocoNet
 * hexfile emulator
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
  */
public class ConnectionConfig extends AbstractSimulatorConnectionConfig<SerialPortAdapter> {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     */
    public ConnectionConfig(SerialPortAdapter p) {
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
        return "LocoNet Simulator"; // NOI18N
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new LnHexFilePort();
        }
    }

}
