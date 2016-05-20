package jmri.jmrix.easydcc.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

import jmri.jmrix.easydcc.EasyDccTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring EasyDccTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision$
 */
public class EasyDccTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public EasyDccTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.easydcc.configurexml.EasyDccTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element turnouts) {
        // create the master object
        EasyDccTurnoutManager.instance();
        // load individual turnouts
        return loadTurnouts(turnouts);
    }

	// initialize logging
    static Logger log = LoggerFactory.getLogger(EasyDccTurnoutManagerXml.class.getName());
}
