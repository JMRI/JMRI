package jmri.jmrix.dcc4pc.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring Dcc4PcSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Kevin Dickerson Copyright: (c) 2012
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 * @version $Revision: 17977 $
 */
public class Dcc4PcSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public Dcc4PcSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class","jmri.jmrix.dcc4pc.configurexml.Dcc4PcSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // create the master object
        //Dcc4PcSensorManager.instance();
        // load individual sensors
        return loadSensors(sensors);
    }

    static Logger log = Logger.getLogger(Dcc4PcSensorManagerXml.class.getName());
}
