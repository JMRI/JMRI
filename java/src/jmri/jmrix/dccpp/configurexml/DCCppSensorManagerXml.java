package jmri.jmrix.dccpp.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring DCCppSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Paul Bender Copyright (c) 2003
 * @author Mark Underwood Copyright (c) 2015
 * @version $Revision$
 */
public class DCCppSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public DCCppSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", "jmri.jmrix.dccpp.configurexml.DCCppSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // load individual sensors
        return loadSensors(sensors);
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppTurnoutManagerXml.class.getName());

    @Override
    public boolean load(Element sharedSensors, Element perNodeSensors) throws JmriConfigureXmlException {
        return this.loadSensors(sharedSensors);
    }

}
