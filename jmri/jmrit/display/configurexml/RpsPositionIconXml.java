package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.RpsPositionIcon;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for rps.RpsPositionIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2006
 * @version $Revision: 1.3 $
 */
public class RpsPositionIconXml implements XmlAdapter {

    public RpsPositionIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * RpsPositionIcon
     * @param o Object to store, of type RpsPositionIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        RpsPositionIcon p = (RpsPositionIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("sensoricon");

        // include contents
        element.setAttribute("x", ""+p.getX());
        element.setAttribute("y", ""+p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));
        element.setAttribute("active", p.getActiveIcon().getName());
        element.setAttribute("error", p.getErrorIcon().getName());
        element.setAttribute("rotate", String.valueOf(p.getActiveIcon().getRotation()));
        element.setAttribute("forcecontroloff", p.getForceControlOff()?"true":"false");
        element.setAttribute("momentary", p.getMomentary()?"true":"false");

        element.setAttribute("sxscale", ""+p.getXScale());
        element.setAttribute("syscale", ""+p.getYScale());
        element.setAttribute("sxorigin", ""+p.getXOrigin());
        element.setAttribute("syorigin", ""+p.getYOrigin());
        
        element.setAttribute("showid", p.isShowID()?"true":"false");

        if (p.getFilter() > 0) 
            element.setAttribute("filter", ""+p.getFilter());
        element.setAttribute("class", "jmri.jmrit.display.configurexml.RpsPositionIconXml");

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

        RpsPositionIcon l = new RpsPositionIcon();

        NamedIcon active;
        name = element.getAttribute("active").getValue();
        l.setActiveIcon(active = CatalogPane.getIconByName(name));

        NamedIcon unknown;
        name = element.getAttribute("error").getValue();
        l.setErrorIcon(unknown = CatalogPane.getIconByName(name));

        try {
            Attribute a = element.getAttribute("rotate");
            if (a!=null) {
                int rotation = element.getAttribute("rotate").getIntValue();
                active.setRotation(rotation, l);
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

        a = element.getAttribute("showid");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setShowID(true);
        else
            l.setShowID(false);

        // find coordinates
        int x = 0;
        int y = 0;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert positional attributes");
        }
        l.setLocation(x,y);

        // find coordinates
        int filter = -1;
        a = element.getAttribute("filter");
        if (a!=null) {
            try {
                filter = a.getIntValue();
            } catch ( org.jdom.DataConversionException e) {
                log.error("failed to convert filter attribute");
            }
            l.setFilter(filter);
        }
        
        double sxScale = 0.;
        double syScale = 0.;
        int sxOrigin = 0;
        int syOrigin = 0;
        try {
            sxScale = element.getAttribute("sxscale").getDoubleValue();
            syScale = element.getAttribute("syscale").getDoubleValue();
            sxOrigin = element.getAttribute("sxorigin").getIntValue();
            syOrigin = element.getAttribute("syorigin").getIntValue();
        } catch ( NullPointerException e1) {
            log.error("missing transform attribute");
        } catch ( org.jdom.DataConversionException e2) {
            log.error("failed to convert transform attributes");
        }
        l.setTransform(sxScale, syScale, sxOrigin, syOrigin);
        
        // find display level
        int level = PanelEditor.SENSORS.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);

        p.putLabel(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RpsPositionIconXml.class.getName());

}