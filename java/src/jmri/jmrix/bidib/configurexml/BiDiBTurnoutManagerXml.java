package jmri.jmrix.bidib.configurexml;

import jmri.InstanceManager;
import jmri.jmrix.bidib.BiDiBSystemConnectionMemo;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.bidib.BiDiBTurnoutManager;

/**
 * Provides load and store functionality for configuring BiDiBTurnoutManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @author Eckart Meyer Copyright (C) 2019
 */
public class BiDiBTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public BiDiBTurnoutManagerXml() {
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
        log.debug("load {} {}", shared, perNode);
        // We tell the Turnout managers that we will be loading turnouts from XML and they should
        // expect additional property set sequences. This is somewhat tricky in the face of
        // possibly multiple connections registered.
        for (BiDiBSystemConnectionMemo memo : InstanceManager.getList(BiDiBSystemConnectionMemo.class)) {
            if (!memo.getDisabled()) {
                ((BiDiBTurnoutManager)memo.getTurnoutManager()).startLoad();
            }
        }
        // load individual turnouts
        boolean result = loadTurnouts(shared, perNode);
        
        // Notifies turnout managers that the loading of XML is complete.
        for (BiDiBSystemConnectionMemo memo : InstanceManager.getList(BiDiBSystemConnectionMemo.class)) {
            if (!memo.getDisabled()) {
                ((BiDiBTurnoutManager)memo.getTurnoutManager()).finishLoad();
            }
        }

        return result;
    }

    private final static Logger log = LoggerFactory.getLogger(BiDiBTurnoutManagerXml.class);

}
