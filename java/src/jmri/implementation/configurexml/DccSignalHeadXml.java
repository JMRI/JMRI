// DccSignalHeadXml.java

package jmri.implementation.configurexml;

import org.apache.log4j.Logger;
import jmri.InstanceManager;
import jmri.implementation.DccSignalHead;

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
        
        return element;
    }

    /**
     * Create a LsDecSignalHead
     * @param element Top level Element to unpack.
     * @return true if successful
     */
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
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static Logger log = Logger.getLogger(DccSignalHeadXml.class.getName());
}
