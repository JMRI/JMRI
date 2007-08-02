package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.SensorIcon;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.SensorIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.16 $
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

        SensorIcon p = (SensorIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("sensoricon");

        // include contents
        element.setAttribute("sensor", p.getSensor().getSystemName());
        element.setAttribute("x", ""+p.getX());
        element.setAttribute("y", ""+p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));
        element.setAttribute("active", p.getActiveIcon().getName());
        element.setAttribute("inactive", p.getInactiveIcon().getName());
        element.setAttribute("unknown", p.getUnknownIcon().getName());
        element.setAttribute("inconsistent", p.getInconsistentIcon().getName());
        element.setAttribute("rotate", String.valueOf(p.getActiveIcon().getRotation()));
        element.setAttribute("forcecontroloff", p.getForceControlOff()?"true":"false");
        element.setAttribute("momentary", p.getMomentary()?"true":"false");

        element.setAttribute("class", "jmri.jmrit.display.configurexml.SensorIconXml");

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

        NamedIcon active;
        name = element.getAttribute("active").getValue();
        l.setActiveIcon(active = CatalogPane.getIconByName(name));

        NamedIcon inactive;
        name = element.getAttribute("inactive").getValue();
        l.setInactiveIcon(inactive = CatalogPane.getIconByName(name));

        NamedIcon unknown;
        name = element.getAttribute("unknown").getValue();
        l.setUnknownIcon(unknown = CatalogPane.getIconByName(name));

        NamedIcon inconsistent;
        name = element.getAttribute("inconsistent").getValue();
        l.setInconsistentIcon(inconsistent = CatalogPane.getIconByName(name));

        try {
            Attribute a = element.getAttribute("rotate");
            if (a!=null) {
                int rotation = element.getAttribute("rotate").getIntValue();
                active.setRotation(rotation, l);
                inactive.setRotation(rotation, l);
                inconsistent.setRotation(rotation, l);
                unknown.setRotation(rotation, l);
            }
        } catch (org.jdom.DataConversionException e) {}

        Attribute a = element.getAttribute("forcecontroloff");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setForceControlOff(true);
        else
            l.setForceControlOff(false);
            
        a = element.getAttribute("momentary");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setMomentary(true);
        else
            l.setMomentary(false);
            
        l.setSensor(element.getAttribute("sensor").getValue());

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

        // find display level
        int level = PanelEditor.SENSORS.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);

        p.putSensor(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorIconXml.class.getName());

}