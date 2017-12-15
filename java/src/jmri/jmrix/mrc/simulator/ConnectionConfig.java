package jmri.jmrix.mrc.simulator;

import javax.swing.JPanel;
import jmri.jmrix.AbstractSimulatorConnectionConfig;
import jmri.jmrix.SerialPortAdapter;

/**
 * Definition of objects to handle configuring a layout connection via an MRC
 * SerialDriverAdapter object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003 Copies from NCE
 * @author kcameron Copyright (C) 2014
 */
public class ConnectionConfig extends AbstractSimulatorConnectionConfig<SerialPortAdapter> {

    public final static String NAME = "Simulator";//IN18N

    /**
     * Ctor for an object being created during load process; Swing init is
     * deferred.
     * @param p serial port adapter
     */
    public ConnectionConfig(SerialPortAdapter p) {
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

    @Override
    public void loadDetails(JPanel details) {
        super.loadDetails(details);
    }

    @Override
    protected void setInstance() {
        if (adapter == null) {
            adapter = new SimulatorAdapter();
        }
    }
}
