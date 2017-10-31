package jmri.jmrix.anyma_dmx.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for
 * configuring AnymaDMX_TurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @author George Warner Copyright (C) 2017
 * @since       4.9.6
 */
public class AnymaDMX_TurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public AnymaDMX_TurnoutManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.anyma_dmx.configurexml.AnymaDMX_TurnoutManagerXml");
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual turnouts
        return loadTurnouts(shared, perNode);
    }

    private final static Logger log = LoggerFactory.getLogger(AnymaDMX_TurnoutManagerXml.class);

}
