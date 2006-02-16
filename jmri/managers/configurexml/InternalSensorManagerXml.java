package jmri.managers.configurexml;

import jmri.managers.InternalSensorManager;

import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring InternalSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2006
 * @version $Revision: 1.1 $
 */
public class InternalSensorManagerXml extends jmri.configurexml.AbstractSensorManagerConfigXML {

    public InternalSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.addAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element sensors) {
        // load individual sensors
        loadSensors(sensors);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(InternalSensorManagerXml.class.getName());
}