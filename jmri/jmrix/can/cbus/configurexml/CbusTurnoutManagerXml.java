package jmri.jmrix.can.cbus.configurexml;

import org.jdom.Element;
import jmri.jmrix.can.cbus.CbusTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring CbusTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @version $Revision: 1.1 $
 * @since 2.3.1
 */
public class CbusTurnoutManagerXml extends jmri.configurexml.AbstractTurnoutManagerConfigXML {

    public CbusTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class",this.getClass().getName());
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public void load(Element turnouts) {
        // load individual turnouts
        loadTurnouts(turnouts);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CbusTurnoutManagerXml.class.getName());

}