package jmri.jmrix.jmriclient.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring JMRIClientLightManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @version $Revision$
 */
public class JMRIClientLightManagerXml extends jmri.managers.configurexml.AbstractLightManagerConfigXML {

    public JMRIClientLightManagerXml() {
        super();
    }

    public void setStoreElementClass(Element lights) {
        lights.setAttribute("class","jmri.jmrix.jmriclient.configurexml.JMRIClientLightManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element lights) {
        // load individual lights
        return loadLights(lights);
    }

	// initialize logging
    static Logger log = Logger.getLogger(JMRIClientLightManagerXml.class.getName());
}
