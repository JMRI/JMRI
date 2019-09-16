package jmri.jmrix.openlcb.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.openlcb.OlcbConfigurationManager;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring OlcbTurnoutManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008, 2010
 * @since 2.3.1
 */
public class OlcbTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public OlcbTurnoutManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", this.getClass().getName());
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // We tell the Turnout managers that we will be loading turnouts from XML and they should
        // expect additional property set sequences. This is somewhat tricky in the face of
        // possibly multiple OpenLCB buses registered.
        for (OlcbConfigurationManager cfg : InstanceManager.getList(OlcbConfigurationManager
                .class)) {
            cfg.getTurnoutManager().startLoad();
        }

        // load individual turnouts
        boolean ret = loadTurnouts(shared, perNode);

        // Notifies OpenLCB turnout managers that the loading of XML is complete.
        for (OlcbConfigurationManager cfg : InstanceManager.getList(OlcbConfigurationManager
                .class)) {
            cfg.getTurnoutManager().finishLoad();
        }
        return ret;
    }

    private final static Logger log = LoggerFactory.getLogger(OlcbTurnoutManagerXml.class);

}
