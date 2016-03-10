package jmri.jmrix.dccpp.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring DCCppTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @author Mark Underwood Copyright: Copyright (c) 2015
 * @version $Revision$
 */
public class DCCppTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public DCCppTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", "jmri.jmrix.dccpp.configurexml.DCCppTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element turnouts) {
        // load individual turnouts
        return loadTurnouts(turnouts, null);
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppTurnoutManagerXml.class.getName());

    @Override
    public boolean load(Element shared, Element perNode) {
        return this.loadTurnouts(shared, perNode);
    }

}
