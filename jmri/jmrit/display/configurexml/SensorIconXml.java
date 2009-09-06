package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.SensorIcon;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.SensorIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.29 $
 */
public class SensorIconXml extends PositionableLabelXml {

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
        element.setAttribute("sensor", p.getSensor().getSystemName());
        storeCommonAttributes(p, element);

        element.setAttribute("active", p.getActiveIcon().getURL());
        element.setAttribute("inactive", p.getInactiveIcon().getURL());
        element.setAttribute("unknown", p.getUnknownIcon().getURL());
        element.setAttribute("inconsistent", p.getInconsistentIcon().getURL());
        element.setAttribute("rotate", String.valueOf(p.getActiveIcon().getRotation()));
        element.setAttribute("momentary", p.getMomentary()?"true":"false");
        storeTextInfo(p, element);

        element.addContent(storeIcon("active", p.getActiveIcon()));
        element.addContent(storeIcon("inactive", p.getInactiveIcon()));
        element.addContent(storeIcon("unknown", p.getUnknownIcon()));
        element.addContent(storeIcon("inconsistent", p.getInconsistentIcon()));

        element.setAttribute("class", "jmri.jmrit.display.configurexml.SensorIconXml");
        return element;
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
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
        l.setActiveIcon(active = NamedIcon.getIconByName(name));

        NamedIcon inactive;
        name = element.getAttribute("inactive").getValue();
        l.setInactiveIcon(inactive = NamedIcon.getIconByName(name));

        NamedIcon unknown;
        name = element.getAttribute("unknown").getValue();
        l.setUnknownIcon(unknown = NamedIcon.getIconByName(name));

        NamedIcon inconsistent;
        name = element.getAttribute("inconsistent").getValue();
        l.setInconsistentIcon(inconsistent = NamedIcon.getIconByName(name));

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

        loadCommonAttributes(l, PanelEditor.SENSORS.intValue(), element);

        Attribute a = element.getAttribute("momentary");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setMomentary(true);
        else
            l.setMomentary(false);
            
        a = element.getAttribute("text");
        if (a!=null) {
            l.setText(a.getValue());
            loadTextInfo(l, element);
        }


        l.setSensor(element.getAttribute("sensor").getValue());

        NamedIcon icon = loadIcon( l,"active", element);
        if (icon!=null) { l.setActiveIcon(icon); }

        icon = loadIcon( l,"inactive", element);
        if (icon!=null) { l.setInactiveIcon(icon); }

        icon = loadIcon( l,"unknown", element);
        if (icon!=null) { l.setUnknownIcon(icon); }

        icon = loadIcon( l,"inconsistent", element);
        if (icon!=null) { l.setInconsistentIcon(icon); }

        p.putLabel(l);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SensorIconXml.class.getName());

}