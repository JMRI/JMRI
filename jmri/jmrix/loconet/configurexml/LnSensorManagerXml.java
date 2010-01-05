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
 * @version $Revision: 1.8 $
 */
public class LnSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public LnSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
    	boolean result = true;
        // create the master object
        LnSensorManager mgr = null;
        try { 
            mgr = LnSensorManager.instance();
        } catch (Exception e) {
            creationErrorEncountered (org.apache.log4j.Level.ERROR,
                                      "Could not create LocoNet Sensor Manager",
                                      null,null,null);
            
            return false;
        }
        // load individual sensors
        result = loadSensors(sensors);
		// Request the status of these sensors from the layout, if appropriate.
		mgr.updateAll();
		return result;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LnSensorManagerXml.class.getName());
}