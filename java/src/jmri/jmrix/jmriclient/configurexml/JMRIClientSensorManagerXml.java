package jmri.jmrix.jmriclient.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring JMRIClientSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @version $Revision$
 */
public class JMRIClientSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public JMRIClientSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class","jmri.jmrix.jmriclient.configurexml.JMRIClientSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // load individual sensors 
        return loadSensors(sensors);
    }

	// initialize logging
    static Logger log = Logger.getLogger(JMRIClientSensorManagerXml.class.getName());
}
