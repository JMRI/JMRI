package jmri.jmrix.xpa.configurexml;

import org.jdom.Element;
import jmri.InstanceManager;
import jmri.jmrix.xpa.XpaTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring XpaTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Paul Bender Copyright (c) 2004
 * @version $Revision: 1.1 $
 */
public class XpaTurnoutManagerXml extends jmri.configurexml.AbstractTurnoutManagerConfigXML {

    public XpaTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.addAttribute("class","jmri.jmrix.xpa.configurexml.XpaTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element turnouts) {
        // create the master object
        XpaTurnoutManager mgr = XpaTurnoutManager.instance();
        // load individual turnouts
        loadTurnouts(turnouts);
    }

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XpaTurnoutManagerXml.class.getName());
}
