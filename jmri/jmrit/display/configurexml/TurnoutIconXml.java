package jmri.jmrit.display.configurexml;

import org.jdom.Element;

import jmri.InstanceManager;
import jmri.jmrit.display.PositionableLabel;
import jmri.configurexml.XmlAdapter;

import jmri.jmrit.display.TurnoutIcon;
import jmri.jmrit.display.PanelEditor;

import javax.swing.*;

/**
 * Handle configuration for display.TurnoutIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.2 $
 */
public class TurnoutIconXml implements XmlAdapter {

    public TurnoutIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * TurnoutIcon
     * @param o Object to store, of type TurnoutIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element element = new Element("turnouticon");
        element.addAttribute("class", "jmri.jmrit.display.configurexml.TurnoutIconXml");

        // include contents
        TurnoutIcon p = (TurnoutIcon)o;
        element.addAttribute("x", ""+p.getX());
        element.addAttribute("y", ""+p.getY());
        element.addAttribute("height", ""+p.getHeight());
        element.addAttribute("width", ""+p.getWidth());
        element.addAttribute("closed", p.getClosedIcon().getName());
        element.addAttribute("thrown", p.getThrownIcon().getName());
        element.addAttribute("unknown", p.getUnknownIcon().getName());
        element.addAttribute("inconsistent", p.getInconsistentIcon().getName());
        element.addAttribute("turnout", p.getTurnout().getSystemName());

        return element;
    }


    public void load(Element element) {
        log.error("Invalid method called");
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  PanelEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        PanelEditor p = (PanelEditor)o;
        String name;

        TurnoutIcon l = new TurnoutIcon();

        name = element.getAttribute("closed").getValue();
        l.setClosedIcon(p.catalog.getIconByName(name));

        name = element.getAttribute("thrown").getValue();
        l.setThrownIcon(p.catalog.getIconByName(name));

        name = element.getAttribute("unknown").getValue();
        l.setUnknownIcon(p.catalog.getIconByName(name));

        name = element.getAttribute("inconsistent").getValue();
        l.setInconsistentIcon(p.catalog.getIconByName(name));

        l.setTurnout(element.getAttribute("turnout").getValue(), null);

        // find coordinates
        int x = 0;
        int y = 0;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        l.setLocation(x,y);
        l.setSize(l.getPreferredSize().width, l.getPreferredSize().height);
        p.putTurnout(l);

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TurnoutIconXml.class.getName());

}