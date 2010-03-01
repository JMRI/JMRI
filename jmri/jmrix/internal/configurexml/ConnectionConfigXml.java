package jmri.jmrix.internal.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.configurexml.AbstractSerialConnectionConfigXml;
import jmri.jmrix.internal.ConnectionConfig;

import org.jdom.*;

/**
 * Handle XML persistance of virtual layout connections
 * <P>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write,
 * as that class is the one actually registered. Reads are brought
 * here directly via the class attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2010
 * @version $Revision: 1.3 $
 */
public class ConnectionConfigXml extends AbstractSerialConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    protected void getInstance() {
        log.error("unexpected call to getInstance");
        new Exception().printStackTrace();
    }

    public Element store(Object o) {
        Element e = new Element("connection");

        e.setAttribute("class", this.getClass().getName());

        return e;
    }
    /**
     * Port name carries the hostname for the network connection
     * @param e Top level Element to unpack.
      */
    public boolean load(Element e) {
    	boolean result = true;
        // configure the other instance objects
        jmri.InstanceManager.setPowerManager(new jmri.managers.DefaultPowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.managers.InternalTurnoutManager());

        jmri.InstanceManager.setSensorManager(new jmri.managers.InternalSensorManager());

       // Install a debug programmer
        jmri.InstanceManager.setProgrammerManager(
                new jmri.progdebugger.DebugProgrammerManager());

        // Install a debug throttle manager
        jmri.InstanceManager.setThrottleManager(
                new jmri.jmrix.debugthrottle.DebugThrottleManager());

        // register, so can be picked up
        register();
        return result;
    }

    protected void register() {
        InstanceManager.configureManagerInstance().registerPref(new ConnectionConfig());
    }

    // initialize logging
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ConnectionConfigXml.class.getName());

}