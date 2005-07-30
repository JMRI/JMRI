package jmri.configurexml;

import jmri.LocoAddress;
import jmri.DccLocoAddress;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for LocoAddress objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2005
 * @version $Revision: 1.1 $
 */
public class LocoAddressXml implements XmlAdapter {

    public LocoAddressXml() {}

    /**
     * Default implementation for storing the contents of a
     * LocoAddress
     * @param o Object to store, of type LocoAddress
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        LocoAddress p = (LocoAddress)o;

        Element element = new Element("locoaddress");

        // include contents
        DccLocoAddressXml adapter = new DccLocoAddressXml();
        
        element.addContent(adapter.store(p));

        return element;
    }

    public void load(Element element) {
        log.error("Invalid method called");
    }
    
    public LocoAddress getAddress(Element element) {
        DccLocoAddressXml adapter = new DccLocoAddressXml();
        return adapter.getAddress(element.getChild("dcclocoaddress"));
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DccLocoAddressXml.class.getName());
}