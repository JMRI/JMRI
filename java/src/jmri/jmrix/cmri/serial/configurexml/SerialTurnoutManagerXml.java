package jmri.jmrix.cmri.serial.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

import jmri.jmrix.cmri.serial.*;

/**
 * Provides load and store functionality for
 * configuring SerialTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 17977 $
 */
public class SerialTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public SerialTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.cmri.serial.configurexml.SerialTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element turnouts) {
        // create the master object
        SerialTurnoutManager.instance();
        // load individual turnouts
        return loadTurnouts(turnouts);
    }

    static Logger log = LoggerFactory.getLogger(SerialTurnoutManagerXml.class.getName());
}
