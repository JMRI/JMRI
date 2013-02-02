package jmri.jmrix.marklin.configurexml;

import org.apache.log4j.Logger;
import org.jdom.Element;

/**
 * Provides load and store functionality for
 * configuring MarklinTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002, 2008
 * @version $Revision: 17977 $
 */
public class MarklinTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public MarklinTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.marklin.configurexml.MarklinTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element turnouts) {
        // create the master object
        //MarklinTurnoutManager.instance();
        // load individual turnouts
        return loadTurnouts(turnouts);
    }

    static Logger log = Logger.getLogger(MarklinTurnoutManagerXml.class.getName());
}
