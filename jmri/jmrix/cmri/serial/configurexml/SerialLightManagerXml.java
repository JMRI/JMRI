// SerialLightManagerXml.java

package jmri.jmrix.cmri.serial.configurexml;

import org.jdom.Element;
import jmri.InstanceManager;
import jmri.jmrix.cmri.serial.*;

/**
 * Provides load and store functionality for
 * configuring SerialLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 * <P>
 * Based on SerialTurnoutManagerXml.java
 *
 * @author Dave Duchamp Copyright (c) 2004
 * @version $Revision: 1.1 $
 */
public class SerialLightManagerXml extends jmri.configurexml.AbstractLightManagerConfigXML {

    public SerialLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.addAttribute("class","jmri.jmrix.cmri.serial.configurexml.SerialLightManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element lights) {
        // create the master object
        SerialLightManager mgr = SerialLightManager.instance();
        // load individual lights
        loadLights(lights);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SerialLightManagerXml.class.getName());
}