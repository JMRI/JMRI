package jmri.jmrix.sprog.configurexml;

import org.jdom.Element;
import jmri.InstanceManager;
import jmri.jmrix.sprog.SprogTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring SprogTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.1 $
 */
public class SprogTurnoutManagerXml extends jmri.configurexml.AbstractTurnoutManagerConfigXML {

    public SprogTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.addAttribute("class","jmri.jmrix.sprog.configurexml.SprogTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element turnouts) {
        // create the master object
        SprogTurnoutManager mgr = new SprogTurnoutManager();
        replaceTurnoutManager(mgr);
        // load individual turnouts
        loadTurnouts(turnouts);
    }

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SprogTurnoutManagerXml.class.getName());
}