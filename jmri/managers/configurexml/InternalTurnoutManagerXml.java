package jmri.managers.configurexml;

import org.jdom.Element;
import jmri.InstanceManager;
import jmri.managers.InternalTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring InternalTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2006
 * @version $Revision: 1.1 $
 */
public class InternalTurnoutManagerXml extends jmri.configurexml.AbstractTurnoutManagerConfigXML {

    public InternalTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.addAttribute("class","jmri.managers.configurexml.InternalTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element turnouts) {
        // load individual turnouts
        loadTurnouts(turnouts);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(InternalTurnoutManagerXml.class.getName());

}