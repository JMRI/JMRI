package jmri.jmrix.lenz.configurexml;

import org.jdom.Element;
import jmri.InstanceManager;
import jmri.jmrix.lenz.XNetTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring XNetTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.4 $
 */
public class XNetTurnoutManagerXml extends jmri.configurexml.AbstractTurnoutManagerConfigXML {

    public XNetTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.addAttribute("class","jmri.jmrix.lenz.configurexml.XNetTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element turnouts) {
        // create the master object
        XNetTurnoutManager mgr = XNetTurnoutManager.instance();
        // load individual turnouts
        loadTurnouts(turnouts);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XNetTurnoutManagerXml.class.getName());

}