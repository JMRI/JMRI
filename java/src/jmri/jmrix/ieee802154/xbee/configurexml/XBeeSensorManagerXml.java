package jmri.jmrix.ieee802154.xbee.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring XBeeSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Ken Cameron Copyright: Copyright (c) 2014
 * @version $Revision$
 */
public class XBeeSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public XBeeSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // load individual sensors
        return loadSensors(sensors);
    }

    static Logger log = LoggerFactory.getLogger(XBeeSensorManagerXml.class.getName());
}

