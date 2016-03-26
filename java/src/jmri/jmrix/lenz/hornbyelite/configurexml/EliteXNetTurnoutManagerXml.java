package jmri.jmrix.lenz.hornbyelite.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring XNetTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Paul Bender Copyright: Copyright (c) 2008
 * @version $Revision$
 */
public class EliteXNetTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public EliteXNetTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", "jmri.jmrix.lenz.configurexml.XNetTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual turnouts
        return loadTurnouts(shared, perNode);
    }

    private final static Logger log = LoggerFactory.getLogger(EliteXNetTurnoutManagerXml.class.getName());

}
