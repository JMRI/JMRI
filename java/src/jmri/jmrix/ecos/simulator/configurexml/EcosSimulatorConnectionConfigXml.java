package jmri.jmrix.ecos.simulator.configurexml;

import jmri.jmrix.ecos.networkdriver.configurexml.ConnectionConfigXml;
import jmri.jmrix.ecos.simulator.EcosSimulatorConnectionConfig;
import jmri.jmrix.ecos.simulator.EcosSimulatorAdapter;

/**
 * Handle XML persistence of layout connections by persisting the
 * SerialDriverAdapter (and connections).
 * <p>
 * Note this is named as the XML version of a EcosSimulatorConnectionConfig
 * object, but it's actually persisting the SerialDriverAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright (c) 2003 copied from NCE/Tams code
 * @author kcameron Copyright (c) 2014
 */
public class EcosSimulatorConnectionConfigXml extends ConnectionConfigXml {

    public EcosSimulatorConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((EcosSimulatorConnectionConfig) object).getAdapter();
    }

    @Override
    protected void getInstance() {
        adapter = new EcosSimulatorAdapter();
    }

    @Override
    protected void register() {
        this.register(new EcosSimulatorConnectionConfig(adapter));
    }

}