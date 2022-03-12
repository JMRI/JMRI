package jmri.jmrix.loconet.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring LnStringIOManagers.
 *
 * @author Bob Jacobsen      Copyright: Copyright (c) 2003
 * @author Daniel Bergqvist  Copyright: Copyright (c) 2021
 */
public class LnStringIOManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public LnStringIOManagerXml() {
        super();
    }

    @Override
    public Element store(Object o) {
        // Don't do anything now. Implement this method later when LnStringIOs
        // can be stored. The only StringIO in this manager is currently the
        // LnThrottleStringIO which shouldn't be stored at all.
        return null;
    }

    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        // Don't do anything now. Implement this method later when LnStringIOs
        // can be stored. The only StringIO in this manager is currently the
        // LnThrottleStringIO which shouldn't be stored at all.
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnStringIOManagerXml.class);
}
