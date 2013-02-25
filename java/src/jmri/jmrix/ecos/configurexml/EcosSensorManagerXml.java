package jmri.jmrix.ecos.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring EcosSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 * @version $Revision$
 */
public class EcosSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public EcosSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class","jmri.jmrix.ecos.configurexml.EcosSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // create the master object
        //EcosSensorManager.instance();
        // load individual sensors
        return loadSensors(sensors);
    }

    static Logger log = LoggerFactory.getLogger(EcosSensorManagerXml.class.getName());
}
