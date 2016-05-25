// AcelaLightManagerXml.java
package jmri.jmrix.acela.configurexml;

import jmri.jmrix.acela.AcelaLightManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring AcelaLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 * <P>
 * @author Dave Duchamp Copyright (c) 2006
 * @version $Revision$
 *
 * @author Bob Coleman, Copyright (c) 2007, 2008 Based on Loconet example,
 * modified to establish Acela support.
 */
public class AcelaLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public AcelaLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class", "jmri.jmrix.acela.configurexml.AcelaLightManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // create the master object
        AcelaLightManager.instance();
        // load individual lights
        return loadLights(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaLightManagerXml.class.getName());
}

/* @(#)AcelaLightManagerXml.java */
