package jmri.jmrix.tams.configurexml;

import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring TamsTurnoutManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * Based on work by Bob Jacobsen
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class TamsTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public TamsTurnoutManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", "jmri.jmrix.tams.configurexml.TamsTurnoutManagerXml");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // create the master object
        //TamsTurnoutManager.instance();
        // load individual turnouts
        return loadTurnouts(shared, perNode);
    }

//    private final static Logger log = LoggerFactory.getLogger(TamsTurnoutManagerXml.class);
}
