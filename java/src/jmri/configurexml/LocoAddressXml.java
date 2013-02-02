package jmri.configurexml;

import org.apache.log4j.Logger;
import jmri.LocoAddress;

import org.jdom.Element;

/**
 * Handle XML configuration for LocoAddress objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2005
 * @version $Revision$
 */
public class LocoAddressXml extends jmri.configurexml.AbstractXmlAdapter {

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
        
        // include contents, we shall also store the old format for backward compatability
        DccLocoAddressXml adapter = new DccLocoAddressXml();
        
        element.addContent(adapter.store(p));
        
        if (p!=null) {
            element.addContent(new Element("number").addContent(""+p.getNumber()));
            element.addContent(new Element("protocol").addContent(p.getProtocol().getShortName()));
        } else {
            element.addContent(new Element("number").addContent(""));
            element.addContent(new Element("protocol").addContent(""));
        }

        return element;
    }

    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }
    
    public LocoAddress getAddress(Element element) {
        if(element.getChild("number")==null){
            DccLocoAddressXml adapter = new DccLocoAddressXml();
            return adapter.getAddress(element.getChild("dcclocoaddress"));
        }
        int addr = 0;
        try {
            addr = Integer.parseInt(element.getChild("number").getText());
        } catch (java.lang.NumberFormatException e){
            return null;
        }
        String protocol = element.getChild("protocol").getText();
        LocoAddress.Protocol prot = LocoAddress.Protocol.getByShortName(protocol);
        return new jmri.DccLocoAddress(addr, prot);
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static Logger log = Logger.getLogger(DccLocoAddressXml.class.getName());
}
