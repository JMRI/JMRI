package jmri.jmrix.jmriclient.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring
 * JMRIClientSensorManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 */
public class JMRIClientSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public JMRIClientSensorManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", "jmri.jmrix.jmriclient.configurexml.JMRIClientSensorManagerXml");
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // load individual sensors 
        return loadSensors(shared);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(JMRIClientSensorManagerXml.class);
}
