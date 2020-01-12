package jmri.jmrix.dcc4pc.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring Dcc4PcSensorManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Kevin Dickerson Copyright: (c) 2012
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 */
public class Dcc4PcSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public Dcc4PcSensorManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", "jmri.jmrix.dcc4pc.configurexml.Dcc4PcSensorManagerXml");
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // create the master object
        //Dcc4PcSensorManager.instance();
        // load individual sensors
        return loadSensors(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(Dcc4PcSensorManagerXml.class);
}
