package jmri.jmrix.jmriclient.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring
 * JMRIClientLightManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 */
public class JMRIClientLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public JMRIClientLightManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.jmriclient.configurexml.JMRIClientLightManagerXml");
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

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(JMRIClientLightManagerXml.class);
}
