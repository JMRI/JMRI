package jmri.jmrix.cmri.serial.configurexml;

import jmri.jmrix.cmri.serial.SerialTurnoutManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring SerialTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision$
 */
public class SerialTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public SerialTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", "jmri.jmrix.cmri.serial.configurexml.SerialTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // create the master object
        SerialTurnoutManager.instance();
        // load individual turnouts
        return loadTurnouts(shared, perNode);
    }

    static Logger log = LoggerFactory.getLogger(SerialTurnoutManagerXml.class.getName());
}
