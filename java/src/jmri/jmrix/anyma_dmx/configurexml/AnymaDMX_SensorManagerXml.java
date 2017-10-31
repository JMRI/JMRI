package jmri.jmrix.anyma_dmx.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for
 * configuring AnymaDMX_SensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author  Paul Bender Copyright (c) 2003
 * @author George Warner Copyright (C) 2017
 * @since       4.9.6
 */
public class AnymaDMX_SensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public AnymaDMX_SensorManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class","jmri.jmrix.anyma_dmx.configurexml.AnymaDMX_SensorManagerXml");
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

    private final static Logger log = LoggerFactory.getLogger(AnymaDMX_TurnoutManagerXml.class);

}
