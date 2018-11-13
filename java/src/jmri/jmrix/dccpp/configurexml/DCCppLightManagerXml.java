package jmri.jmrix.dccpp.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring DCCppLightManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Dave Duchamp Copyright (c) 2006
 * @author Mark Underwood Copyright (c) 2006
 *
 * Based on XNetLightManagerXml by Dave Duchamp
 */
public class DCCppLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public DCCppLightManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.dccpp.configurexml.DCCppLightManagerXml");
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element lights) {
        // load individual lights
        return loadLights(lights);
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        return loadLights(shared);
    }
    
    private final static Logger log = LoggerFactory.getLogger(DCCppLightManagerXml.class);

}
