package jmri.jmrix.rps.configurexml;

import org.apache.log4j.Logger;
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
 * @version $Revision$
 */
public class RpsSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public RpsSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // create the master object
        RpsSensorManager.instance();
        // load individual sensors
        return loadSensors(sensors);
    }

    static Logger log = Logger.getLogger(RpsSensorManagerXml.class.getName());
}
