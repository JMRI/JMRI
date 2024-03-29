package jmri.jmrix.lenz.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring XNetSensorManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Paul Bender Copyright (c) 2003
 */
public class XNetSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public XNetSensorManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", "jmri.jmrix.lenz.configurexml.XNetSensorManagerXml");
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // load individual sensors
        return loadSensors(shared);
    }

//    private static final Logger log = LoggerFactory.getLogger(XNetSensorManagerXml.class);

}
