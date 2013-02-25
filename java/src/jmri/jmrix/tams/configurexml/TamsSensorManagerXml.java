package jmri.jmrix.tams.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring TamsSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version $Revision: 17977 $
 */
public class TamsSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public TamsSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class","jmri.jmrix.tams.configurexml.TamsSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // create the master object
        //TamsSensorManager.instance();
        // load individual sensors
        return loadSensors(sensors);
    }

    static Logger log = LoggerFactory.getLogger(TamsSensorManagerXml.class.getName());
}
