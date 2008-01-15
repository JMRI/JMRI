// SerialLightManagerXml.java

package jmri.jmrix.powerline.configurexml;

import org.jdom.Element;
import jmri.jmrix.powerline.*;

/**
 * Provides load and store functionality for
 * configuring SerialLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 * <P>
 * Based on SerialTurnoutManagerXml.java
 *
 * @author Dave Duchamp Copyright (c) 2004, 2007, 2008
 * @version $Revision: 1.1 $
 */
public class SerialLightManagerXml extends jmri.configurexml.AbstractLightManagerConfigXML {

    public SerialLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class","jmri.jmrix.powerline.configurexml.SerialLightManagerXml");
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