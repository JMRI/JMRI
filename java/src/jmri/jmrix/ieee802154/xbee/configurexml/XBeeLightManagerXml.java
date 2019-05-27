package jmri.jmrix.ieee802154.xbee.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring XBeeLightManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Ken Cameron Copyright: Copyright (c) 2014
 */
public class XBeeLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public XBeeLightManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", this.getClass().getName());
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

    private final static Logger log = LoggerFactory.getLogger(XBeeLightManagerXml.class);
}
