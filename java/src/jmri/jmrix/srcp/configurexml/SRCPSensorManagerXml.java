package jmri.jmrix.srcp.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

import jmri.jmrix.srcp.SRCPSensorManager;

/**
 * Provides load and store functionality for
 * configuring SRCPSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2013
 * @version $Revision$
 */
public class SRCPSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public SRCPSensorManagerXml() {
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
        SRCPSensorManager.instance();
        // load individual sensors
        return loadSensors(sensors);
    }

    static Logger log = LoggerFactory.getLogger(SRCPSensorManagerXml.class.getName());
}
