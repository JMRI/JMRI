package jmri.jmrix.lenz.hornbyelite.configurexml;

import org.jdom.Element;

import jmri.jmrix.lenz.hornbyelite.EliteXNetTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring XNetTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Paul Bender Copyright: Copyright (c) 2008
 * @version $Revision: 1.1 $
 */
public class EliteXNetTurnoutManagerXml extends jmri.configurexml.AbstractTurnoutManagerConfigXML {

    public EliteXNetTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.lenz.configurexml.XNetTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element turnouts) {
        // create the master object
        jmri.jmrix.lenz.XNetTurnoutManager mgr = jmri.jmrix.lenz.XNetTurnoutManager.instance();
        // load individual turnouts
        loadTurnouts(turnouts);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(EliteXNetTurnoutManagerXml.class.getName());

}
