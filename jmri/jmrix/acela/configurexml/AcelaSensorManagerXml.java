// AcelaSensorManagerXml.java

package jmri.jmrix.acela.configurexml;

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
 * @version     $Revision: 1.1 $
 *
 * @author      Bob Coleman, Copyright (c) 2007, 2008
 *              Based on CMRI serial example, modified to establish Acela support. 
 */

public class AcelaSensorManagerXml extends jmri.configurexml.AbstractSensorManagerConfigXML {

    public AcelaSensorManagerXml() {
        super();
    }

    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class","jmri.jmrix.acela.configurexml.AcelaSensorManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element sensors) {
        // create the master object
        AcelaSensorManager mgr = AcelaSensorManager.instance();
        
        // load individual sensors
        loadSensors(sensors);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AcelaSensorManagerXml.class.getName());
}

/* @(#)AcelaSensorManagerXml.java */