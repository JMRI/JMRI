// LnLightManagerXml.java

package jmri.jmrix.loconet.configurexml;

import org.jdom.Element;
import jmri.InstanceManager;
import jmri.jmrix.loconet.*;

/**
 * Provides load and store functionality for
 * configuring LnLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 * <P>
 * @author Dave Duchamp Copyright (c) 2006
 * @version $Revision: 1.1 $
 */
public class LnLightManagerXml extends jmri.configurexml.AbstractLightManagerConfigXML {

    public LnLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.addAttribute("class","jmri.jmrix.loconet.configurexml.LnLightManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element lights) {
        // create the master object
        LnLightManager mgr = LnLightManager.instance();
        // load individual lights
        loadLights(lights);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnLightManagerXml.class.getName());
}