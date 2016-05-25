package jmri.jmrix.dcc.configurexml;

import jmri.jmrix.dcc.DccTurnoutManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring DccTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2014
 * @version $Revision$
 */
public class DccTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public DccTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", "jmri.jmrix.dcc.configurexml.DccTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // create the master object
        DccTurnoutManager.instance();
        // load individual turnouts
        return loadTurnouts(shared, perNode);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(DccTurnoutManagerXml.class.getName());
}
