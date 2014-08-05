package jmri.jmrix.mrc.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;


/**
 * New MRC Xml Turnout Manager
 * From Xpa
 * Provides load and store functionality for
 * configuring NceTurnoutManagers.
 * <P>
 * Uses the store method from the abstract base class, but
 * provides a load method here.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @author Martin Wade Copyright (C) 2014
 * @version $Revision: 22821 $
 */
public class MrcTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public MrcTurnoutManagerXml() {
        super();
    }

    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class","jmri.jmrix.mrc.configurexml.MrcTurnoutManagerXml");//IN18N
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");//IN18N
    }

    public boolean load(Element turnouts) {
        // load individual turnouts
        return loadTurnouts(turnouts);
    }

    static Logger log = LoggerFactory.getLogger(MrcTurnoutManagerXml.class.getName());
}
