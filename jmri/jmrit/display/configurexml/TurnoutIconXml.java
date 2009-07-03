package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPanel;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.TurnoutIcon;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.TurnoutIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.22 $
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

        TurnoutIcon p = (TurnoutIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("turnouticon");

        // include contents
        element.setAttribute("turnout", p.getTurnout().getSystemName());
        element.setAttribute("x", ""+p.getX());
        element.setAttribute("y", ""+p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));
        element.setAttribute("closed", p.getClosedIcon().getURL());
        element.setAttribute("thrown", p.getThrownIcon().getURL());
        element.setAttribute("unknown", p.getUnknownIcon().getURL());
        element.setAttribute("inconsistent", p.getInconsistentIcon().getURL());
        element.setAttribute("rotate", String.valueOf(p.getClosedIcon().getRotation()));
        element.setAttribute("forcecontroloff", p.getForceControlOff()?"true":"false");
        element.setAttribute("tristate", p.getTristate()?"true":"false");
        
        element.setAttribute("class", "jmri.jmrit.display.configurexml.TurnoutIconXml");

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
    @SuppressWarnings("null")
	public void load(Element element, Object o) {
        // create the objects
        PanelEditor p = (PanelEditor)o;
        String name;

        TurnoutIcon l = new TurnoutIcon();

        NamedIcon closed;
        name = element.getAttribute("closed").getValue();
        l.setClosedIcon(closed = CatalogPanel.getIconByName(name));
        if (closed == null) log.warn("did not locate closed icon file "+name);

        NamedIcon thrown;
        name = element.getAttribute("thrown").getValue();
        l.setThrownIcon(thrown = CatalogPanel.getIconByName(name));
        if (thrown == null) log.warn("did not locate thrown icon file "+name);

        NamedIcon unknown;
        name = element.getAttribute("unknown").getValue();
        l.setUnknownIcon(unknown = CatalogPanel.getIconByName(name));
        if (unknown == null) log.warn("did not locate unknown icon file "+name);

        NamedIcon inconsistent;
        name = element.getAttribute("inconsistent").getValue();
        l.setInconsistentIcon(inconsistent = CatalogPanel.getIconByName(name));
        if (inconsistent == null) log.warn("did not locate inconsistent icon file "+name);

        try {
            Attribute a = element.getAttribute("rotate");
            if (a!=null) {
                int rotation = element.getAttribute("rotate").getIntValue();
                closed.setRotation(rotation, l);
                thrown.setRotation(rotation, l);
                inconsistent.setRotation(rotation, l);
                unknown.setRotation(rotation, l);
            }
        } catch (org.jdom.DataConversionException e) {}

        Attribute a = element.getAttribute("forcecontroloff");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setForceControlOff(true);
        else
            l.setForceControlOff(false);
        
        a = element.getAttribute("tristate");
        if ( (a==null) || ((a!=null) && a.getValue().equals("true")))
            l.setTristate(true);
        else
            l.setTristate(false);
            
        l.setTurnout(element.getAttribute("turnout").getValue());

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
        int level = PanelEditor.TURNOUTS.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);

        p.putLabel(l);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TurnoutIconXml.class.getName());
}