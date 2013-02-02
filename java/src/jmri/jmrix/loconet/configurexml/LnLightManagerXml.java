// LnLightManagerXml.java

package jmri.jmrix.loconet.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring LnLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 * <P>
 * @author Dave Duchamp Copyright (c) 2006
 * @version $Revision$
 */
public class LnLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public LnLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class","jmri.jmrix.loconet.configurexml.LnLightManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element lights) {
        // load individual lights
        return loadLights(lights);
    }

    static Logger log = Logger.getLogger(LnLightManagerXml.class.getName());
}
