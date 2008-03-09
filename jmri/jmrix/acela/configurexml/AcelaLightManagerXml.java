// AcelaLightManagerXml.java

package jmri.jmrix.acela.configurexml;

import org.jdom.Element;
import jmri.jmrix.acela.*;

/**
 * Provides load and store functionality for
 * configuring AcelaLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 * <P>
 * @author      Dave Duchamp Copyright (c) 2006
 * @version     $Revision: 1.1 $
 *
 * @author      Bob Coleman, Copyright (c) 2007, 2008
 *              Based on Loconet example, modified to establish Acela support. 
 */
public class AcelaLightManagerXml extends jmri.configurexml.AbstractLightManagerConfigXML {

    public AcelaLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class","jmri.jmrix.acela.configurexml.AcelaLightManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element lights) {
        // create the master object
        AcelaLightManager mgr = AcelaLightManager.instance();
        // load individual lights
        loadLights(lights);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AcelaLightManagerXml.class.getName());
}

/* @(#)AcelaLightManagerXml.java */