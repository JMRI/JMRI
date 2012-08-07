package jmri.jmrix.marklin.configurexml;

import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring MarklinSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 * @version $Revision: 17977 $
 */
public class MarklinSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public MarklinSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class","jmri.jmrix.marklin.configurexml.MarklinSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // create the master object
        //MarklinSensorManager.instance();
        // load individual sensors
        return loadSensors(sensors);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MarklinSensorManagerXml.class.getName());
}