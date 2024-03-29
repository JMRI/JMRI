package jmri.jmrix.lenz.configurexml;

import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring XNetLightManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Dave Duchamp Copyright (c) 2006
 */
public class XNetLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public XNetLightManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.lenz.configurexml.XNetLightManagerXml");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual lights
        return loadLights(shared);
    }

//    private static final Logger log = LoggerFactory.getLogger(XNetLightManagerXml.class);

}
