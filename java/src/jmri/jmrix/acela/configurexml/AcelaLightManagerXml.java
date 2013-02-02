// AcelaLightManagerXml.java

package jmri.jmrix.acela.configurexml;

import org.apache.log4j.Logger;
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
 * @version     $Revision$
 *
 * @author      Bob Coleman, Copyright (c) 2007, 2008
 *              Based on Loconet example, modified to establish Acela support. 
 */
public class AcelaLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public AcelaLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class","jmri.jmrix.acela.configurexml.AcelaLightManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element lights) {
        // create the master object
        AcelaLightManager.instance();
        // load individual lights
        return loadLights(lights);
    }

    static Logger log = Logger.getLogger(AcelaLightManagerXml.class.getName());
}

/* @(#)AcelaLightManagerXml.java */
