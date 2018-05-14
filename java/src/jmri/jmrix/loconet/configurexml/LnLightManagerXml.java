package jmri.jmrix.loconet.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring LnLightManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Dave Duchamp Copyright (c) 2006
 */
public class LnLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public LnLightManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.loconet.configurexml.LnLightManagerXml"); // NOI18N
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual lights
        return loadLights(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(LnLightManagerXml.class);
}
