package jmri.jmrix.easydcc.configurexml;

import jmri.jmrix.easydcc.EasyDccTurnoutManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring EasyDccTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision$
 */
public class EasyDccTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public EasyDccTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", "jmri.jmrix.easydcc.configurexml.EasyDccTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // create the master object
        EasyDccTurnoutManager.instance();
        // load individual turnouts
        return loadTurnouts(shared, perNode);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(EasyDccTurnoutManagerXml.class.getName());
}
