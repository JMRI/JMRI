package jmri.jmrix.nce.configurexml;

import org.jdom.Element;
import jmri.InstanceManager;
import jmri.jmrix.nce.*;

/**
 * Provides load and store functionality for
 * configuring NceSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.1 $
 */
public class NceSensorManagerXml extends jmri.configurexml.AbstractSensorManagerConfigXML {

    public NceSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.addAttribute("class","jmri.jmrix.nce.NceSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element sensors) {
        // create the master object
        NceSensorManager mgr = NceSensorManager.instance();
        // load individual sensors
        loadSensors(sensors);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NceSensorManagerXml.class.getName());
}