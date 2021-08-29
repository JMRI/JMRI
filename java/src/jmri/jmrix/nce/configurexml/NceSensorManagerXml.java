package jmri.jmrix.nce.configurexml;

import org.jdom2.Element;

import jmri.configurexml.JmriConfigureXmlException;

/**
 * Provides load and store functionality for configuring NceSensorManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 */
public class NceSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public NceSensorManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName());
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // load individual sensors
        return loadSensors(shared);
    }

//    private final static Logger log = LoggerFactory.getLogger(NceSensorManagerXml.class);
}
