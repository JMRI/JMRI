// SerialLightManagerXml.java
package jmri.jmrix.grapevine.configurexml;

import jmri.jmrix.grapevine.SerialLightManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring SerialLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 * <P>
 * Based on SerialTurnoutManagerXml.java
 *
 * @author Dave Duchamp Copyright (c) 2004, 2007
 * @version $Revision$
 */
public class SerialLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public SerialLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.grapevine.configurexml.SerialLightManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // create the master object
        SerialLightManager.instance();
        // load individual lights
        return loadLights(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialLightManagerXml.class.getName());
}
