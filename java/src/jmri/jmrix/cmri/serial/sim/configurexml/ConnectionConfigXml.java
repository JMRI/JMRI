package jmri.jmrix.cmri.serial.sim.configurexml;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.jmrix.cmri.serial.sim.ConnectionConfig;
import jmri.jmrix.cmri.serial.sim.SimDriverAdapter;

/**
 * Handle XML persistance of layout connections by persisting the
 * SimDriverAdapter (and connections). Note this is named as the XML version of
 * a ConnectionConfig object, but it's actually persisting the SimDriverAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 * <p>
 * This inherits from the cmri.serial.serialdriver version, so it can reuse (and
 * benefit from changes to) that code.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 */
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS") // OK by convention
public class ConnectionConfigXml extends jmri.jmrix.cmri.serial.serialdriver.configurexml.ConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if(adapter == null) {
           adapter = new SimDriverAdapter();
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
