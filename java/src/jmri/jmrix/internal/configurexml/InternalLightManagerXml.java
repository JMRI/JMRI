package jmri.jmrix.internal.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring InternalLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2009
 */
public class InternalLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public InternalLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual lights
        loadLights(shared);
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(InternalLightManagerXml.class.getName());
}
