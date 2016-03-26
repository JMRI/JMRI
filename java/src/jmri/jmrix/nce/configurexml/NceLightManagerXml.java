// NceLightManagerXml.java
package jmri.jmrix.nce.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring NceLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 * <P>
 * @author Dave Duchamp Copyright (c) 2010
 * @version $Revision$
 */
public class NceLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public NceLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.nce.configurexml.NceLightManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual lights
        return loadLights(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(NceLightManagerXml.class.getName());
}
