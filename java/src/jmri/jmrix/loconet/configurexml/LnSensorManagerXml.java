package jmri.jmrix.loconet.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring LnSensorManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 */
public class LnSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public LnSensorManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName()); // NOI18N
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // load individual sensors
        loadSensors(shared);
        return true;
    }

    private final static Logger log = LoggerFactory.getLogger(LnSensorManagerXml.class);
}
