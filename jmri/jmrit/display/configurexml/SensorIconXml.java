package jmri.jmrit.display.configurexml;

import org.jdom.Element;

import jmri.InstanceManager;
import jmri.jmrit.display.PositionableLabel;
import jmri.configurexml.XmlAdapter;

import jmri.jmrit.display.SensorIcon;
import jmri.jmrit.display.PanelEditor;

import javax.swing.*;

/**
 * Handle configuration for display.SensorIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.1 $
 */
public class SensorIconXml implements XmlAdapter {

    public SensorIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * SensorIcon
     * @param o Object to store, of type SensorIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        Element element = new Element("sensoricon");
        element.addAttribute("class", "jmri.jmrit.display.configurexml.SensorIconXml");

        // include contents
        SensorIcon p = (SensorIcon)o;
        element.addAttribute("x", ""+p.getX());
        element.addAttribute("y", ""+p.getY());
        element.addAttribute("height", ""+p.getHeight());
        element.addAttribute("width", ""+p.getWidth());
        element.addAttribute("active", p.getActiveIcon().getName());
        element.addAttribute("inactive", p.getInactiveIcon().getName());
        element.addAttribute("unknown", p.getUnknownIcon().getName());
        element.addAttribute("inconsistent", p.getInconsistentIcon().getName());
        element.addAttribute("sensor", p.getSensor().getID());

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

        SensorIcon l = new SensorIcon();

        name = element.getAttribute("active").getValue();
        l.setActiveIcon(p.catalog.getIconByName(name));

        name = element.getAttribute("inactive").getValue();
        l.setInactiveIcon(p.catalog.getIconByName(name));

        name = element.getAttribute("unknown").getValue();
        l.setUnknownIcon(p.catalog.getIconByName(name));

        name = element.getAttribute("inconsistent").getValue();
        l.setInconsistentIcon(p.catalog.getIconByName(name));

        l.setSensor(element.getAttribute("sensor").getValue(), "");

        // find coordinates
        int x = 0;
        int y = 0;
        int height = 10;
        int width = 10;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
            height = element.getAttribute("height").getIntValue();
            width = element.getAttribute("width").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        l.setLocation(x,y);
        l.setSize(width, height);
        p.putSensor(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorIconXml.class.getName());

}