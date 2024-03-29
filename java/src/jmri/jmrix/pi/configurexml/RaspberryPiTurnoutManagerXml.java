package jmri.jmrix.pi.configurexml;

import org.jdom2.Element;

/**
 * Provides load and store functionality for
 * configuring RaspberryPiTurnoutManagers.
 * <p>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 */
public class RaspberryPiTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public RaspberryPiTurnoutManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", "jmri.jmrix.pi.configurexml.RaspberryPiTurnoutManagerXml");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual turnouts
        return loadTurnouts(shared, perNode);
    }

//    private final static Logger log = LoggerFactory.getLogger(RaspberryPiTurnoutManagerXml.class);

}
