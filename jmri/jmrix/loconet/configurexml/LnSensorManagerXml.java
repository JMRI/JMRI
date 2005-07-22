package jmri.jmrix.loconet.configurexml;

import jmri.jmrix.loconet.LnSensorManager;

import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring LnSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.3 $
 */
public class LnSensorManagerXml extends jmri.configurexml.AbstractSensorManagerConfigXML {

    public LnSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.addAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element sensors) {
        // create the master object
        LnSensorManager mgr = LnSensorManager.instance();
        // load individual sensors
        loadSensors(sensors);
		// Request the status of these sensors from the layout, if appropriate.
		mgr.updateAll();
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LnSensorManagerXml.class.getName());
}