package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.SignalHeadIcon;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.SignalHeadIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.15 $
 */
public class SignalHeadIconXml implements XmlAdapter {

    public SignalHeadIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * SignalHeadIcon
     * @param o Object to store, of type SignalHeadIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        SignalHeadIcon p = (SignalHeadIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("signalheadicon");

        // include contents
        element.addAttribute("signalhead", ""+p.getSignalHead().getSystemName());
        element.addAttribute("x", ""+p.getX());
        element.addAttribute("y", ""+p.getY());
        element.addAttribute("level", String.valueOf(p.getDisplayLevel()));
        element.addAttribute("dark", p.getDarkIcon().getName());
        element.addAttribute("red", p.getRedIcon().getName());
        element.addAttribute("yellow", p.getYellowIcon().getName());
        element.addAttribute("flashyellow", p.getFlashYellowIcon().getName());
        element.addAttribute("green", p.getGreenIcon().getName());
        element.addAttribute("flashred", p.getFlashRedIcon().getName());
        element.addAttribute("flashgreen", p.getFlashGreenIcon().getName());
        element.addAttribute("rotate", String.valueOf(p.getGreenIcon().getRotation()));
        element.addAttribute("forcecontroloff", p.getForceControlOff()?"true":"false");

        element.addAttribute("class", "jmri.jmrit.display.configurexml.SignalHeadIconXml");

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

        SignalHeadIcon l = new SignalHeadIcon();
        // handle old format!
        if (element.getAttribute("signalhead") == null) {
            log.error("incorrect information for signal head; must use signalhead name");
            return;
        }

        l.setSignalHead(element.getAttribute("signalhead").getValue());

        NamedIcon red;
        name = element.getAttribute("red").getValue();
        l.setRedIcon(red = CatalogPane.getIconByName(name));

        NamedIcon yellow;
        name = element.getAttribute("yellow").getValue();
        l.setYellowIcon(yellow = CatalogPane.getIconByName(name));

        NamedIcon green;
        name = element.getAttribute("green").getValue();
        l.setGreenIcon(green = CatalogPane.getIconByName(name));

        Attribute a; 

        NamedIcon dark = null;
        a = element.getAttribute("dark");
        if (a!=null) 
            l.setDarkIcon(dark = CatalogPane.getIconByName(a.getValue()));

        NamedIcon flashred = null;
        a = element.getAttribute("flashred");
        if (a!=null) 
            l.setFlashRedIcon(flashred = CatalogPane.getIconByName(a.getValue()));

        NamedIcon flashyellow = null;
        a = element.getAttribute("flashyellow");
        if (a!=null) 
            l.setFlashYellowIcon(flashyellow = CatalogPane.getIconByName(a.getValue()));

        NamedIcon flashgreen = null;
        a = element.getAttribute("flashgreen");
        if (a!=null) 
            l.setFlashGreenIcon(flashgreen = CatalogPane.getIconByName(a.getValue()));
        
        try {
            a = element.getAttribute("rotate");
            if (a!=null) {
                int rotation = element.getAttribute("rotate").getIntValue();
                red.setRotation(rotation, l);
                yellow.setRotation(rotation, l);
                 green.setRotation(rotation, l);
                if (flashred!=null) flashred.setRotation(rotation, l);
                if (flashyellow!=null) flashyellow.setRotation(rotation, l);
                if (flashgreen!=null) flashgreen.setRotation(rotation, l);
                if (dark!=null) dark.setRotation(rotation, l);
            }
        } catch (org.jdom.DataConversionException e) {}

        a = element.getAttribute("forcecontroloff");
        if ( (a!=null) && a.getValue().equals("true"))
            l.setForceControlOff(true);
        else
            l.setForceControlOff(false);
            
        l.displayState(l.headState());

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
        int level = PanelEditor.SIGNALS.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);

        p.putSignal(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SignalHeadIconXml.class.getName());

}