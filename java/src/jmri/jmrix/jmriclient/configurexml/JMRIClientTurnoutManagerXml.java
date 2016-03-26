package jmri.jmrix.jmriclient.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides load and store functionality for configuring
 * JMRIClientTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2008
 * @version $Revision$
 */
public class JMRIClientTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public JMRIClientTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", "jmri.jmrix.jmriclient.configurexml.JMRIClientTurnoutManagerXml");
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual turnouts
        return loadTurnouts(shared, perNode);
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(JMRIClientTurnoutManagerXml.class.getName());
}
