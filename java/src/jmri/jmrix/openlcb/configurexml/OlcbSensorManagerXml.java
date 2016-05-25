package jmri.jmrix.openlcb.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring OlcbSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008, 2010
 * @version $Revision$
 * @since 2.3.1
 */
public class OlcbSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public OlcbSensorManagerXml() {
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
        boolean result = true;
        // create the master object
        //OlcbSensorManager mgr = OlcbSensorManager.instance();
        // load individual sensors
        result = loadSensors(shared);
        // Request the status of these sensors from the layout, if appropriate.
        //mgr.updateAll();
        return result;
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbSensorManagerXml.class.getName());
}
