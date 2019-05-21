package jmri.jmrix.loconet.hexfile;

/**
 * Definition of objects to handle configuring a layout connection via a LocoNet
 * hexfile emulator.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003
 */
public class ConnectionConfig extends jmri.jmrix.AbstractSimulatorConnectionConfig {

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     *
     * @param p a {@link jmri.jmrix.SerialPortAdapter} object
     */
    public ConnectionConfig(jmri.jmrix.SerialPortAdapter p) {
        super(p);
    }

    public ConnectionConfig() {
        super();
    }

    @Override
    public String name() {
        return "LocoNet Simulator"; // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new LnHexFilePort();
        }
    }

}
