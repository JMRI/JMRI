package jmri.jmrix.lenz.configurexml;

import org.jdom.Element;
import jmri.InstanceManager;
import jmri.jmrix.lenz.XNetSensorManager;

/**
 * Provides load and store functionality for
 * configuring XNetSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author  Paul Bender Copyright (c) 2003
 * @version $Revision: 1.1 $
 */
public class XNetSensorManagerXml extends jmri.configurexml.AbstractSensorManagerConfigXML {

    public XNetSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.addAttribute("class","jmri.jmrix.lenz.configurexml.XNetSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element sensors) {
        // create the master object
        XNetSensorManager mgr = XNetSensorManager.instance();
        // load individual sensors
        loadSensors(sensors);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTurnoutManagerXml.class.getName());

}
