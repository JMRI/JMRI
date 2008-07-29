package jmri.jmrix.acela.configurexml;

import org.jdom.Element;

import jmri.jmrix.acela.*;

/**
 * Provides load and store functionality for
 * configuring AcelaTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @version $Revision: 1.1 $
 */
public class AcelaTurnoutManagerXml extends jmri.configurexml.AbstractTurnoutManagerConfigXML {

    public AcelaTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.acela.configurexml.AcelaTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element turnouts) {
        // create the master object
        AcelaTurnoutManager mgr = AcelaTurnoutManager.instance();
        // load individual turnouts
        loadTurnouts(turnouts);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AcelaTurnoutManagerXml.class.getName());
}