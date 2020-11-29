package jmri.jmrix.dccpp.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring DCCppTurnoutManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @author Mark Underwood Copyright (c) 2015
 */
public class DCCppTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public DCCppTurnoutManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", "jmri.jmrix.dccpp.configurexml.DCCppTurnoutManagerXml");
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element turnouts) {
        // load individual turnouts
        return loadTurnouts(turnouts, null);
    }

    private static final Logger log = LoggerFactory.getLogger(DCCppTurnoutManagerXml.class);

    @Override
    public boolean load(Element shared, Element perNode) {
        return this.loadTurnouts(shared, perNode);
    }

}
