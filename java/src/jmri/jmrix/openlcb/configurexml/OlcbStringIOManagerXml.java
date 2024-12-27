package jmri.jmrix.openlcb.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring LnStringIOManagers.
 *
 * @author Bob Jacobsen      Copyright: Copyright (c) 2003
 * @author Daniel Bergqvist  Copyright: Copyright (c) 2021
 */
public class OlcbStringIOManagerXml extends jmri.managers.configurexml.AbstractStringIOManagerConfigXML {

    public OlcbStringIOManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element sensors) {
        sensors.setAttribute("class", this.getClass().getName());
    }


    @Override
    public boolean load(Element shared, Element perNode) throws JmriConfigureXmlException {
        boolean load = loadStringIOs(shared);

        return load;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(OlcbStringIOManagerXml.class);
}
