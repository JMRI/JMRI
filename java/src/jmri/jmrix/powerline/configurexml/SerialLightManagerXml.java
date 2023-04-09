package jmri.jmrix.powerline.configurexml;

import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring SerialLightManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 * <p>
 * Based on SerialTurnoutManagerXml.java
 *
 * @author Dave Duchamp Copyright (c) 2004, 2007, 2008
 */
public class SerialLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public SerialLightManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.powerline.configurexml.SerialLightManagerXml");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual lights
        return loadLights(shared);
    }

}
