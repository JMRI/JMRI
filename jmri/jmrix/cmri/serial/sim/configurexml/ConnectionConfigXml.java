package jmri.jmrix.cmri.serial.sim.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractConnectionConfigXml;
import jmri.jmrix.cmri.serial.sim.ConnectionConfig;
import jmri.jmrix.cmri.serial.sim.SerialDriverAdapter;
import jmri.jmrix.cmri.serial.*;
import java.util.List;
import org.jdom.*;

/**
 * Handle XML persistance of layout connections by persisting
 * the SerialDriverAdapter (and connections). Note this is
 * named as the XML version of a ConnectionConfig object,
 * but it's actually persisting the SerialDriverAdapter.
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 * <p>
 * This inherits from the cmri.serial.serialdriver version, so
 * it can reuse (and benefit from changes to) that code.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 * @version $Revision: 1.1 $
 */
public class ConnectionConfigXml extends jmri.jmrix.cmri.serial.serialdriver.configurexml.ConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }
	
    protected void getInstance() {
        adapter = SerialDriverAdapter.instance();
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig(adapter));
    }
     

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConnectionConfigXml.class.getName());

}