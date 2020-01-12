package jmri.jmrix.acela.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide load and store functionality for configuring AcelaLightManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Dave Duchamp Copyright (c) 2006
 * @author Bob Coleman, Copyright (c) 2007, 2008 Based on LocoNet example,
 * modified to establish Acela support.
 */
public class AcelaLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public AcelaLightManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.acela.configurexml.AcelaLightManagerXml");
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

    private final static Logger log = LoggerFactory.getLogger(AcelaLightManagerXml.class);

}
