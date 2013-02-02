// SerialLightManagerXml.java

package jmri.jmrix.cmri.serial.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;

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
 * @version $Revision$
 */
public class SerialLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public SerialLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class","jmri.jmrix.cmri.serial.configurexml.SerialLightManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element lights) {
        // create the master object
        SerialLightManager.instance();
        // load individual lights
        return loadLights(lights);
    }

    static Logger log = Logger.getLogger(SerialLightManagerXml.class.getName());
}
