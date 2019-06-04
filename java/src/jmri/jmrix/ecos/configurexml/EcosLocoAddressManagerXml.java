package jmri.jmrix.ecos.configurexml;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is here to prevent error messages being presented to the user on
 * opening JMRI or saving the panel file, when connected to an Ecos. It
 * currently serves no other function.
 * <p>
 *
 * @author Kevin Dickerson Copyright: Copyright (c) 2009
 */
public class EcosLocoAddressManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public EcosLocoAddressManagerXml() {
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        return true;
    }

    @Override
    public Element store(Object o) {
        return null;
    }
    private final static Logger log = LoggerFactory.getLogger(EcosLocoAddressManagerXml.class);
}
