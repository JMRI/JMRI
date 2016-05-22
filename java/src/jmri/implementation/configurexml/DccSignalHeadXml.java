// DccSignalHeadXml.java

package jmri.implementation.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.InstanceManager;
import jmri.implementation.DccSignalHead;
import java.util.List;

import org.jdom.Element;

/**
 * Handle XML configuration for DccSignalHead objects.
 *
 * This file is part of JMRI.
 * 
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * 
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008, 2009
 * @author Petr Koud'a  Copyright: Copyright (c) 2007
 * @version $Revision$
 */
public class DccSignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public DccSignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * LsDecSignalHead
     * @param o Object to store, of type LsDecSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        DccSignalHead p = (DccSignalHead)o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());
        element.addContent(new Element("systemName").addContent(p.getSystemName()));
        
        storeCommon(p, element);

        if(p.useAddressOffSet())
            element.addContent(new Element("useAddressOffSet").addContent("yes"));
        else
            element.addContent(new Element("useAddressOffSet").addContent("no"));

        for(int i = 0; i<p.getValidStates().length; i++){
            String aspect = p.getValidStateNames()[i];
            //String address = p.getOutputForAppearance(i);
            Element el = new Element("aspect");
            el.setAttribute("defines", aspect);
            el.addContent(new Element("number").addContent(Integer.toString(p.getOutputForAppearance(p.getValidStates()[i]))));
            element.addContent(el);
        }

        return element;
    }
    
    /**
     * Create a LsDecSignalHead
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
    public boolean load(Element element) {
        // put it together
        String sys = getSystemName(element);
        String uname = getUserName(element);
        DccSignalHead h;
        if (uname == null)
            h = new DccSignalHead(sys);
        else
            h = new DccSignalHead(sys, uname);

        loadCommon(h, element);
        
        if ( element.getChild("useAddressOffSet") != null) {
            if(element.getChild("useAddressOffSet").getText().equals("yes"))
                h.useAddressOffSet(true);
        }
        
        List<Element> list = element.getChildren("aspect");
        for (Element e: list) {
            String aspect = e.getAttribute("defines").getValue();
            int number = -1;
            try {
                String value = e.getChild("number").getValue();
                number = Integer.parseInt(value);

            } catch (Exception ex) {
                log.error("failed to convert DCC number");
            }
            int indexOfAspect = -1;

            for(int i = 0; i<h.getValidStates().length; i++){
                if(h.getValidStateNames()[i].equals(aspect)){
                    indexOfAspect = i;
                    break;
                }
            }
            if(indexOfAspect!=-1)
                h.setOutputForAppearance(h.getValidStates()[indexOfAspect], number);
        }
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static Logger log = LoggerFactory.getLogger(DccSignalHeadXml.class.getName());
}
