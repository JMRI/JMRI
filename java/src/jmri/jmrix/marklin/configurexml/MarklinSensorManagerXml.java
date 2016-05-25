package jmri.jmrix.marklin.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring MarklinSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 * @version $Revision: 17977 $
 */
public class MarklinSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public MarklinSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", "jmri.jmrix.marklin.configurexml.MarklinSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // create the master object
        //MarklinSensorManager.instance();
        // load individual sensors
        return loadSensors(shared);
    }

    private final static Logger log = LoggerFactory.getLogger(MarklinSensorManagerXml.class.getName());
}
