package jmri.jmrix.sprog.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;
import jmri.jmrix.sprog.SprogTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring SprogTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision$
 */
public class SprogTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public SprogTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.sprog.configurexml.SprogTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element turnouts) {
        // create the master object
        SprogTurnoutManager.instance();
        // load individual turnouts
        return loadTurnouts(turnouts);
    }

	// initialize logging
    static Logger log = Logger.getLogger(SprogTurnoutManagerXml.class.getName());
}
