package jmri.jmrix.nce.configurexml;

import org.jdom.Element;
import jmri.InstanceManager;
import jmri.jmrix.nce.NceTurnoutManager;

/**
 * Provides load and store functionality for
 * configuring NceTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.1 $
 */
public class NceTurnoutManagerXml extends jmri.configurexml.AbstractTurnoutManagerConfigXML {

    public NceTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.addAttribute("class","jmri.jmrix.nce.configurexml.NceTurnoutManagerXml");
    }

    public void load(Element turnouts) {
        // create the master object
        NceTurnoutManager mgr = new NceTurnoutManager();
        // register it with InstanceManager
        InstanceManager.setTurnoutManager(mgr);
        // register it for configuration
        InstanceManager.configureManagerInstance().register(mgr);
        // load individual turnouts
        loadTurnouts(turnouts);
    }

}