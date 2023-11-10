package jmri.jmrix.ecos.configurexml;

import org.jdom2.Element;

/**
 * This class is here to prevent error messages being presented to the user on
 * opening JMRI or saving the panel file, when connected to an Ecos. It
 * currently serves no other function.
 *
 * @author Kevin Dickerson Copyright: Copyright (c) 2009
 */
public class EcosLocoAddressManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public EcosLocoAddressManagerXml() {
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        return true;
    }

    @Override
    public Element store(Object o) {
        return null;
    }

//    private final static Logger log = LoggerFactory.getLogger(EcosLocoAddressManagerXml.class);
}
