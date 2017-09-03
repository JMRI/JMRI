package jmri.jmrix.roco.z21.simulator.configurexml;

import jmri.jmrix.roco.z21.simulator.Z21SimulatorAdapter;
import jmri.jmrix.roco.z21.simulator.Z21SimulatorConnectionConfig;

/**
 * Handle XML persistence of layout connections by persisting the
 * Z21SimulatorAdapter (and connections).
 * <p>
 * Note this is named as the XML version of a Z21SimulatorConnectionConfig
 * object, but it's actually persisting the Z21SimulatorAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Paul Bender Copyright: Copyright (c) 2009
 */
public class Z21SimulatorConnectionConfigXml extends jmri.jmrix.roco.z21.configurexml.ConnectionConfigXml {

    public Z21SimulatorConnectionConfigXml() {
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
        adapter = ((Z21SimulatorConnectionConfig) object).getAdapter();
    }

    @Override
    protected void register() {
        this.register(new Z21SimulatorConnectionConfig(adapter));
    }

}
