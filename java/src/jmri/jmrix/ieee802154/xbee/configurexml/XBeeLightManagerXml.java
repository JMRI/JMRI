package jmri.jmrix.ieee802154.xbee.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring XBeeLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Ken Cameron Copyright: Copyright (c) 2014
 * @version $Revision$
 */
public class XBeeLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public XBeeLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element lights) {
        // load individual lights
        return loadLights(lights);
    }

    static Logger log = LoggerFactory.getLogger(XBeeLightManagerXml.class.getName());
}

