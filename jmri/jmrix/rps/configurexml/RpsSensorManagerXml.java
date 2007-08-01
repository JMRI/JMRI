package jmri.jmrix.rps.configurexml;

import jmri.jmrix.rps.RpsSensorManager;

import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring RpsSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2007
 * @version $Revision: 1.1 $
 */
public class RpsSensorManagerXml extends jmri.configurexml.AbstractSensorManagerConfigXML {

    public RpsSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element sensors) {
        // create the master object
        RpsSensorManager mgr = RpsSensorManager.instance();
        // load individual sensors
        loadSensors(sensors);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RpsSensorManagerXml.class.getName());
}