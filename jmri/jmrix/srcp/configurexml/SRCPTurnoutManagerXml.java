package jmri.jmrix.srcp.configurexml;

import org.jdom.Element;

import jmri.jmrix.srcp.SRCPTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring SRCPTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @version $Revision: 1.1 $
 */
public class SRCPTurnoutManagerXml extends jmri.configurexml.AbstractTurnoutManagerConfigXML {

    public SRCPTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.srcp.configurexml.SRCPTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element turnouts) {
        // create the master object
        SRCPTurnoutManager mgr = SRCPTurnoutManager.instance();
        // load individual turnouts
        loadTurnouts(turnouts);
    }

	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SRCPTurnoutManagerXml.class.getName());
}