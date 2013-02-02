package jmri.configurexml;

import org.apache.log4j.Logger;
import jmri.DccLocoAddress;

import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for DccLocoAddress objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2005
 * @version $Revision$
 */
public class DccLocoAddressXml extends jmri.configurexml.AbstractXmlAdapter {

    public DccLocoAddressXml() {}

    /**
     * Default implementation for storing the contents of a
     * DccLocoAddress
     * @param o Object to store, of type DccLocoAddress
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        DccLocoAddress p = (DccLocoAddress)o;

        Element element = new Element("dcclocoaddress");

        // include contents
        if (p!=null) {
            element.setAttribute("number", ""+p.getNumber());
            if (p.isLongAddress()) element.setAttribute("longaddress", "yes");
            else element.setAttribute("longaddress", "no");
        } else {
            element.setAttribute("number", "");
            element.setAttribute("longaddress", "no");
        }
        return element;
    }

    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }
    
    public DccLocoAddress getAddress(Element element) {
        if (element.getAttribute("number").getValue().equals(""))
            return null;
        int number = Integer.parseInt(element.getAttribute("number").getValue());
        boolean longaddress = false;
        Attribute a = element.getAttribute("longaddress");
        if (a!=null && a.getValue().equals("yes")) longaddress = true;
        return new DccLocoAddress(number, longaddress);
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static Logger log = Logger.getLogger(DccLocoAddressXml.class.getName());
}
