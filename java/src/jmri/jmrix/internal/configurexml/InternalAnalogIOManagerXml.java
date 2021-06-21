package jmri.jmrix.internal.configurexml;

import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring
 * InternalAnalogIOManager.
 *
 * @author Bob Jacobsen      Copyright: Copyright (c) 2006
 * @author Daniel Bergqvist  Copyright: Copyright (c) 2021
 */
public class InternalAnalogIOManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public InternalAnalogIOManagerXml() {
        super();
    }

    @Override
    public Element store(Object o) {
        return null;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        return true;
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InternalAnalogIOManagerXml.class);

}
