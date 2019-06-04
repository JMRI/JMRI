package jmri.jmrix.tams.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring TamsSensorManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * Based on work by Bob Jacobsen
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 */
public class TamsSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public TamsSensorManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", "jmri.jmrix.tams.configurexml.TamsSensorManagerXml");
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // create the master object
        //TamsSensorManager.instance();
        // load individual sensors
        return loadSensors(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(TamsSensorManagerXml.class);
}
