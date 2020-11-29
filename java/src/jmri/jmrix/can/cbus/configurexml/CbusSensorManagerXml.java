package jmri.jmrix.can.cbus.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring CbusSensorManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @since 2.3.1
 */
public class CbusSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public CbusSensorManagerXml() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // load individual sensors
        // Request the status of these sensors from the layout, if appropriate.
        //mgr.updateAll();
        return loadSensors(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(CbusSensorManagerXml.class);
}
