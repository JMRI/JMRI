package jmri.jmrix.lenz.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring XNetSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author  Paul Bender Copyright (c) 2003
 * @version $Revision$
 */
public class XNetSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public XNetSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class","jmri.jmrix.lenz.configurexml.XNetSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // load individual sensors
        return loadSensors(sensors);
    }

    static Logger log = LoggerFactory.getLogger(XNetTurnoutManagerXml.class.getName());

}
