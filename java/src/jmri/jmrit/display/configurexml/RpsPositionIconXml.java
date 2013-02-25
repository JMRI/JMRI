package jmri.jmrit.display.configurexml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.RpsPositionIcon;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for rps.RpsPositionIcon objects
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2006
 * @version $Revision$
 */
public class RpsPositionIconXml extends PositionableLabelXml {

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
        storeCommonAttributes(p, element);
        // include contents
        element.setAttribute("active", p.getActiveIcon().getURL());
        element.setAttribute("error", p.getErrorIcon().getURL());
        element.setAttribute("rotate", String.valueOf(p.getActiveIcon().getRotation()));
        element.setAttribute("momentary", p.getMomentary()?"true":"false");

        element.setAttribute("sxscale", ""+p.getXScale());
        element.setAttribute("syscale", ""+p.getYScale());
        element.setAttribute("sxorigin", ""+p.getXOrigin());
        element.setAttribute("syorigin", ""+p.getYOrigin());
        
        element.setAttribute("showid", p.isShowID()?"true":"false");

        if (p.getFilter()!=null) 
            element.setAttribute("filter", ""+p.getFilter());

        element.addContent(storeIcon("active", p.getActiveIcon()));
        element.addContent(storeIcon("error", p.getErrorIcon()));

        element.setAttribute("class", "jmri.jmrit.display.configurexml.RpsPositionIconXml");
        return element;
    }


    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     * @param element Top level Element to unpack.
     * @param o  an Editor as an Object
     */
    public void load(Element element, Object o) {
		Editor ed = (Editor)o;
        RpsPositionIcon l = new RpsPositionIcon(ed);

        // create the objects
        String name = element.getAttribute("active").getValue();
        NamedIcon active = NamedIcon.getIconByName(name);
        if (active==null) {
            active = ed.loadFailed("RpsPositionIcon: icon \"active\" ", name);
            if (active==null) {
                log.info("RpsPositionIcon: icon \"active\" removed for url= "+name);
                return;
            }
        }
        l.setActiveIcon(active);

        name = element.getAttribute("error").getValue();
        NamedIcon error = NamedIcon.getIconByName(name);
        if (error==null) {
            error = ed.loadFailed("RpsPositionIcon: icon \"error\" ", name);
            if (error==null) {
                log.info("RpsPositionIcon: \"error\" removed for url= "+name);
                return;
            }
        }
        l.setErrorIcon(error);

        try {
            Attribute a = element.getAttribute("rotate");
            if (a!=null) {
                int rotation = element.getAttribute("rotate").getIntValue();
                active.setRotation(rotation, l);
                error.setRotation(rotation, l);
            }
        } catch (org.jdom.DataConversionException e) {}

        Attribute a = element.getAttribute("momentary");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setMomentary(true);
        else
            l.setMomentary(false);

        a = element.getAttribute("showid");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setShowID(true);
        else
            l.setShowID(false);

        a = element.getAttribute("filter");
        if (a!=null) {
            l.setFilter(a.getValue());
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
        
        NamedIcon icon = loadIcon( l,"active", element, "RpsPositionIcon ", ed);
        if (icon!=null) { l.setActiveIcon(icon); }
        icon = loadIcon( l,"error", element, "RpsPositionIcon ", ed);
        if (icon!=null) { l.setErrorIcon(icon); }
        ed.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.SENSORS, element);
    }

    static Logger log = LoggerFactory.getLogger(RpsPositionIconXml.class.getName());

}
