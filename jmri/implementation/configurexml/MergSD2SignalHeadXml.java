// MergSD2SignalHeadXml.java

package jmri.implementation.configurexml;

import jmri.InstanceManager;
import jmri.SignalHead;
import jmri.implementation.MergSD2SignalHead;
import jmri.Turnout;

import java.util.List;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle XML configuration for MergSD2SignalHead objects.
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
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2008
 * @author Kevin Dickerson  Copyright: Copyright (c) 2009
 * @version $Revision: 1.1 $
 */
public class MergSD2SignalHeadXml extends jmri.managers.configurexml.AbstractNamedBeanManagerConfigXML {

    public MergSD2SignalHeadXml() {}

    /**
     * Default implementation for storing the contents of a
     * MergSD2SignalHead
     * @param o Object to store, of type MergSD2SignalHead
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        MergSD2SignalHead p = (MergSD2SignalHead)o;

        Element element = new Element("signalhead");
        element.setAttribute("class", this.getClass().getName());

        // include contents
        element.setAttribute("systemName", p.getSystemName());
        element.setAttribute("aspects", p.getAspects()+"");
        if(p.getFeather()) element.setAttribute("feather", "yes");
        
        storeCommon(p, element);
        int aspects = p.getAspects();
        
        switch (aspects){
        case 2 :    element.addContent(addSingleTurnoutElement(p.getInput1()));
                    if(!p.getHome()) element.setAttribute("home", "no");
                    break;
        case 3 :    element.addContent(addSingleTurnoutElement(p.getInput1()));
                    element.addContent(addSingleTurnoutElement(p.getInput2()));
                    break;
        case 4 :    element.addContent(addSingleTurnoutElement(p.getInput1()));
                    element.addContent(addSingleTurnoutElement(p.getInput2()));
                    element.addContent(addSingleTurnoutElement(p.getInput3()));
                    break;
        }
        
        return element;
    }


    
    Element addSingleTurnoutElement(Turnout to) {
        String user = to.getUserName();
        String sys = to.getSystemName();
        
        Element el = new Element("turnout");
        el.setAttribute("systemName", sys);
        if (user!=null) el.setAttribute("userName", user);
        return el;
    }
    

    /**
     * Create a MergSD2SignalHead
     * @param element Top level Element to unpack.
     * @return true if successful
     */
    @SuppressWarnings("unchecked")
	public boolean load(Element element) {
        List<Element> l = element.getChildren("turnout");
        Turnout input1 = null;
        Turnout input2 = null;
        Turnout input3 = null;
        String yesno ="";
        boolean feather = false;
        boolean home = true;
        
        // put it together
        String sys = element.getAttribute("systemName").getValue();
        Attribute a = element.getAttribute("userName");
        if (element.getAttribute("feather")!=null) {
            yesno = element.getAttribute("feather").getValue();
        }
        if ( (yesno!=null) && (!yesno.equals("")) ) {
            if (yesno.equals("yes")) feather=true;
            else if (yesno.equals("no")) feather=false;
        }
        
        if (element.getAttribute("home")!=null) {
            yesno = element.getAttribute("home").getValue();
        }
        if ( (yesno!=null) && (!yesno.equals("")) ) {
            if (yesno.equals("yes")) home=true;
            else if (yesno.equals("no")) home=false;
        }
        
        SignalHead h;
        int aspects = Integer.parseInt(element.getAttribute("aspects").getValue());
        
        switch (aspects){
            case 2: input1 = loadTurnout(l.get(0));
                    break;
            case 3: input1 = loadTurnout(l.get(0));
                    input2 = loadTurnout(l.get(1));
                    break;
            case 4: input1 = loadTurnout(l.get(0));
                    input2 = loadTurnout(l.get(1));
                    input3 = loadTurnout(l.get(2));
                    break;
        }
        if (a == null)
            h = new MergSD2SignalHead(sys, aspects, input1, input2, input3, feather, home);
        else
            h = new MergSD2SignalHead(sys, a.getValue(), aspects, input1, input2, input3, feather, home);

        loadCommon(h, element);
        
        InstanceManager.signalHeadManagerInstance().register(h);
        return true;
    }

    Turnout loadTurnout(Object o) {
        Element e = (Element)o;

        // we don't create the Turnout, we just look it up.
        String sys = e.getAttribute("systemName").getValue();
        return InstanceManager.turnoutManagerInstance().getBySystemName(sys);
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MergSD2SignalHeadXml.class.getName());
}
