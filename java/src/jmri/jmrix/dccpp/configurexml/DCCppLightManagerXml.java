package jmri.jmrix.dccpp.configurexml;

import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring DCCppLightManagers.
 * <p>
 * Uses the store and load methods from the abstract base class.
 * Light mode is persisted as a bean property via AbstractNamedBeanManagerConfigXML.
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
    public boolean load(Element lights) {
        // load individual lights
        return loadLights(lights);
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        return loadLights(shared);
    }

}
