package jmri.jmrix.grapevine.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;

/**
 * Provide load and store functionality for configuring SerialSensorManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2006, 2007
 */
public class SerialSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public SerialSensorManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", "jmri.jmrix.grapevine.configurexml.SerialSensorManagerXml");
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // load individual sensors
        return loadSensors(shared);
    }

//    private final static Logger log = LoggerFactory.getLogger(SerialSensorManagerXml.class);

}
