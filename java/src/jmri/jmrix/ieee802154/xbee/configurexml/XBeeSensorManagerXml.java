package jmri.jmrix.ieee802154.xbee.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring XBeeSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Ken Cameron Copyright: Copyright (c) 2014
 * @version $Revision$
 */
public class XBeeSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public XBeeSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // load individual sensors
        return loadSensors(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(XBeeSensorManagerXml.class.getName());
}
