// AcelaSensorManagerXml.java

package jmri.jmrix.acela.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;
import jmri.jmrix.acela.*;

/**
 * Provides load and store functionality for
 * configuring SerialSensorManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author      Bob Jacobsen Copyright: Copyright (c) 2003
 * @version     $Revision$
 *
 * @author      Bob Coleman, Copyright (c) 2007, 2008
 *              Based on CMRI serial example, modified to establish Acela support. 
 */

public class AcelaSensorManagerXml extends jmri.managers.configurexml.AbstractSensorManagerConfigXML {

    public AcelaSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class","jmri.jmrix.acela.configurexml.AcelaSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element sensors) throws jmri.configurexml.JmriConfigureXmlException {
        // create the master object
        try { 
            AcelaSensorManager.instance();
        } catch (Exception e) {
            creationErrorEncountered (org.apache.log4j.Level.ERROR,
                                      "Could not create Acela Sensor Manager",
                                      null,null,null);
            
            return false;
        }

        // load individual sensors
        return loadSensors(sensors);
    }

    static Logger log = Logger.getLogger(AcelaSensorManagerXml.class.getName());
}

/* @(#)AcelaSensorManagerXml.java */
