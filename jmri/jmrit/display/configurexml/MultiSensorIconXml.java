package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.MultiSensorIcon;
import org.jdom.Attribute;
import org.jdom.Element;

import com.sun.java.util.collections.List;

/**
 * Handle configuration for display.MultiSensorIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.1 $
 */
public class MultiSensorIconXml implements XmlAdapter {

    public MultiSensorIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * MultiSensorIcon
     * @param o Object to store, of type MultiSensorIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        MultiSensorIcon p = (MultiSensorIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("multisensoricon");

        // include contents
        element.addAttribute("x", ""+p.getX());
        element.addAttribute("y", ""+p.getY());
        element.addAttribute("level", String.valueOf(p.getDisplayLevel()));
        element.addAttribute("inactive", p.getInactiveIcon().getName());
        element.addAttribute("unknown", p.getUnknownIcon().getName());
        element.addAttribute("inconsistent", p.getInconsistentIcon().getName());
        element.addAttribute("rotate", String.valueOf(p.getUnknownIcon().getRotation()));
        element.addAttribute("updown", p.getUpDown()?"true":"false");
        element.addAttribute("forcecontroloff", p.getForceControlOff()?"true":"false");

        element.addAttribute("class", "jmri.jmrit.display.configurexml.MultiSensorIconXml");

        for (int i = 0; i < p.getNumEntries(); i++) {
            Element e = new Element("multisensoriconentry");
            e.addAttribute("sensor", p.getSensorName(i));
            e.addAttribute("icon", p.getSensorIcon(i).getName());
            element.addContent(e);
        }
        
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

        MultiSensorIcon l = new MultiSensorIcon();

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
                inactive.setRotation(rotation, l);
                inconsistent.setRotation(rotation, l);
                unknown.setRotation(rotation, l);
            }
        } catch (org.jdom.DataConversionException e) {}

        Attribute a = element.getAttribute("updown");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setUpDown(true);
        else
            l.setUpDown(false);
            
        a = element.getAttribute("forcecontroloff");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setForceControlOff(true);
        else
            l.setForceControlOff(false);
            
        // get the icon pairs
        List items = element.getChildren();
        for (int i = 0; i<items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = (Element)items.get(i);
            String sensor = item.getAttribute("sensor").getValue();
            String icon = item.getAttribute("icon").getValue();
            l.addEntry(sensor, CatalogPane.getIconByName(icon));
        }
        
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

        p.putMultiSensor(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(MultiSensorIconXml.class.getName());

}