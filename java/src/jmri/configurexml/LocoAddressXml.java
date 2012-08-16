package jmri.configurexml;

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
            String protocol = null;
            switch(p.getProtocol()){
                case(LocoAddress.DCC_SHORT) : protocol = "dcc_short"; break;
                case(LocoAddress.DCC_LONG) : protocol = "dcc_long"; break;
                case(LocoAddress.DCC) : protocol = "dcc"; break;
                case(LocoAddress.SELECTRIX) : protocol = "selectrix"; break;
                case(LocoAddress.MOTOROLA) : protocol = "motorola"; break;
                case(LocoAddress.MFX) : protocol = "mfx"; break;
                case(LocoAddress.M4) : protocol = "m4"; break;
                default : protocol = "dcc";
            }
            element.addContent(new Element("protocol").addContent(protocol));
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
        int addr = Integer.parseInt(element.getChild("number").getText());
        String protocol = element.getChild("protocol").getText();
        int prot = 0x00;
        if(protocol.equals("dcc_short")){
            prot = LocoAddress.DCC_SHORT;
        } else if (protocol.equals("dcc_long")){
            prot = LocoAddress.DCC_LONG;
        } else if (protocol.equals("dcc")){
            prot = LocoAddress.DCC;
        } else if (protocol.equals("selectrix")){
            prot = LocoAddress.SELECTRIX;
        } else if (protocol.equals("motorola")){
            prot = LocoAddress.MOTOROLA;
        } else if (protocol.equals("mfx")){
            prot = LocoAddress.MFX;
        } else if (protocol.equals("m4")){
            prot = LocoAddress.M4;
        } else {
            prot = LocoAddress.DCC;
        }
        return new jmri.DccLocoAddress(addr, prot);
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DccLocoAddressXml.class.getName());
}