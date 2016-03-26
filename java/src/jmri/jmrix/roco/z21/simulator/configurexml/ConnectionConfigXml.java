package jmri.jmrix.roco.z21.simulator.configurexml;

import jmri.jmrix.roco.z21.simulator.ConnectionConfig;
import jmri.jmrix.roco.z21.simulator.Z21SimulatorAdapter;

/**
 * Handle XML persistence of layout connections by persisting the
 Z21SimulatorAdapter (and connections). Note this is named as the XML version
 of a ConnectionConfig object, but it's actually persisting the
 Z21SimulatorAdapter.
 <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Paul Bender Copyright: Copyright (c) 2009
 */
public class ConnectionConfigXml extends jmri.jmrix.roco.z21.configurexml.ConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if (adapter == null) {
            adapter = new Z21SimulatorAdapter();
        }
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

}
