package jmri.jmrix.easydcc.configurexml;

import org.jdom.Element;
import jmri.InstanceManager;
import jmri.jmrix.easydcc.EasyDccTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring EasyDccTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.3 $
 */
public class EasyDccTurnoutManagerXml extends jmri.configurexml.AbstractTurnoutManagerConfigXML {

    public EasyDccTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.addAttribute("class","jmri.jmrix.easydcc.configurexml.EasyDccTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element turnouts) {
        // create the master object
        EasyDccTurnoutManager mgr = new EasyDccTurnoutManager();
        replaceTurnoutManager(mgr);
        // load individual turnouts
        loadTurnouts(turnouts);
    }

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EasyDccTurnoutManagerXml.class.getName());
}