// EcosLocoAddressManagerXml.java

package jmri.jmrix.ecos.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Element;

/**
 * This class is here to prevent error messages
 * being presented to the user on opening JMRI
 * or saving the panel file, when connected to
 * an Ecos.  It currently serves no other function.
 * <P>
 *
 * @author Kevin Dickerson Copyright: Copyright (c) 2009
 * @version $Revision$
 */

public class EcosLocoAddressManagerXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML{

    public EcosLocoAddressManagerXml() { }
    
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    public boolean load(Element locoaddress) {
        return true;
    }

    public Element store(Object o){
        return null;
    }
    static Logger log = LoggerFactory.getLogger(EcosLocoAddressManagerXml.class.getName());
}
