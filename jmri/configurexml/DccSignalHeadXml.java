// LsDecSignalHeadXml.java

// This file is part of JMRI.
//
// JMRI is free software; you can redistribute it and/or modify it under
// the terms of version 2 of the GNU General Public License as published
// by the Free Software Foundation. See the "COPYING" file for a copy
// of this license.
//
// JMRI is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// for more details.

package jmri.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.DccSignalHead;
import jmri.Turnout;
import java.util.List;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for LsDecSignalHead objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003
 * @author Petr Koud'a  Copyright: Copyright (c) 2007
 * @version $Revision: 1.1 $
 */
public class DccSignalHeadXml implements XmlAdapter {

    public DccSignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * LsDecSignalHead
     * @param o Object to store, of type LsDecSignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        DccSignalHead p = (DccSignalHead)o;

        Element element = new Element("dccsignalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());
        if (p.getUserName() != null) element.setAttribute("userName", p.getUserName());

        return element;
    }

    /**
     * Create a LsDecSignalHead
     * @param element Top level Element to unpack.
     */
    public void load(Element element) {
        // put it together
        String sys = element.getAttribute("systemName").getValue();
        Attribute a = element.getAttribute("userName");
        DccSignalHead h;
        if (a == null)
            h = new DccSignalHead(sys);
        else
            h = new DccSignalHead(sys, a.getValue());
        InstanceManager.signalHeadManagerInstance().register(h);
        return;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DccSignalHeadXml.class.getName());
}
