// LnLightManagerXml.java
package jmri.jmrix.loconet.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring LnLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 * <P>
 * @author Dave Duchamp Copyright (c) 2006
 * @version $Revision$
 */
public class LnLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public LnLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.loconet.configurexml.LnLightManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual lights
        return loadLights(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(LnLightManagerXml.class.getName());
}
