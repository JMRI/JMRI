package jmri.jmrix.rps.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrix.rps.RpsSensorManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring RpsSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2007
 * @version $Revision$
 */
public class RpsSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public RpsSensorManagerXml() {
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
        // create the master object
        RpsSensorManager.instance();
        // load individual sensors
        return loadSensors(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(RpsSensorManagerXml.class.getName());
}
