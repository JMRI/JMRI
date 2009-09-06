// SignalHeadIconXml.java

package jmri.jmrit.display.configurexml;

import jmri.SignalHead;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.SignalHeadIcon;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.SignalHeadIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.30 $
 */
public class SignalHeadIconXml extends PositionableLabelXml {

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
        storeCommonAttributes(p, element);
        element.setAttribute("signalhead", ""+p.getSignalHead().getSystemName());

        element.setAttribute("held", p.getHeldIcon().getURL());
        element.setAttribute("dark", p.getDarkIcon().getURL());
        element.setAttribute("red", p.getRedIcon().getURL());
        element.setAttribute("yellow", p.getYellowIcon().getURL());
        element.setAttribute("flashyellow", p.getFlashYellowIcon().getURL());
        element.setAttribute("green", p.getGreenIcon().getURL());
        element.setAttribute("lunar", p.getLunarIcon().getURL());
        element.setAttribute("flashred", p.getFlashRedIcon().getURL());
        element.setAttribute("flashgreen", p.getFlashGreenIcon().getURL());
        element.setAttribute("flashlunar", p.getFlashLunarIcon().getURL());
        element.setAttribute("rotate", String.valueOf(p.getGreenIcon().getRotation()));
        element.setAttribute("clickmode", ""+p.getClickMode());
        element.setAttribute("litmode", ""+p.getLitMode());

        element.addContent(storeIcon("held", p.getHeldIcon()));
        element.addContent(storeIcon("dark", p.getDarkIcon()));
        element.addContent(storeIcon("red", p.getRedIcon()));
        element.addContent(storeIcon("yellow", p.getYellowIcon()));
        element.addContent(storeIcon("flashyellow", p.getFlashYellowIcon()));
        element.addContent(storeIcon("green", p.getGreenIcon()));
        element.addContent(storeIcon("lunar", p.getLunarIcon()));
        element.addContent(storeIcon("flashred", p.getFlashRedIcon()));
        element.addContent(storeIcon("flashgreen", p.getFlashGreenIcon()));
        element.addContent(storeIcon("flashlunar", p.getFlashLunarIcon()));

        element.setAttribute("class", "jmri.jmrit.display.configurexml.SignalHeadIconXml");
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

        SignalHeadIcon l = new SignalHeadIcon();
        // handle old format!
        Attribute attr = element.getAttribute("signalhead"); 
        if (attr == null) {
            log.error("incorrect information for signal head; must use signalhead name");
            return;
        }
        SignalHead sh = jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(
            attr.getValue());
        if (sh != null) {
            l.setSignalHead(sh);
        } else {
            log.error("SignalHead named '"+attr.getValue()+"' not found.");
            return;
        }


        NamedIcon red;
        name = element.getAttribute("red").getValue();
        l.setRedIcon(red = NamedIcon.getIconByName(name));

        NamedIcon yellow;
        name = element.getAttribute("yellow").getValue();
        l.setYellowIcon(yellow = NamedIcon.getIconByName(name));

        NamedIcon green;
        name = element.getAttribute("green").getValue();
        l.setGreenIcon(green = NamedIcon.getIconByName(name));

        NamedIcon lunar=null;
        if (element.getAttribute("lunar") != null) {
            name = element.getAttribute("lunar").getValue();
            l.setLunarIcon(lunar = NamedIcon.getIconByName(name));
        }

        Attribute a; 

        NamedIcon held = null;
        a = element.getAttribute("held");
        if (a!=null) 
            l.setHeldIcon(held = NamedIcon.getIconByName(a.getValue()));

        NamedIcon dark = null;
        a = element.getAttribute("dark");
        if (a!=null) 
            l.setDarkIcon(dark = NamedIcon.getIconByName(a.getValue()));

        NamedIcon flashred = null;
        a = element.getAttribute("flashred");
        if (a!=null) 
            l.setFlashRedIcon(flashred = NamedIcon.getIconByName(a.getValue()));

        NamedIcon flashyellow = null;
        a = element.getAttribute("flashyellow");
        if (a!=null) 
            l.setFlashYellowIcon(flashyellow = NamedIcon.getIconByName(a.getValue()));

        NamedIcon flashgreen = null;
        a = element.getAttribute("flashgreen");
        if (a!=null) 
            l.setFlashGreenIcon(flashgreen = NamedIcon.getIconByName(a.getValue()));
        
        NamedIcon flashlunar = null;
        a = element.getAttribute("flashlunar");
        if (a!=null) 
            l.setFlashLunarIcon(flashlunar = NamedIcon.getIconByName(a.getValue()));
        
        try {
            a = element.getAttribute("rotate");
            if (a!=null) {
                int rotation = a.getIntValue();
                red.setRotation(rotation, l);
                yellow.setRotation(rotation, l);
                green.setRotation(rotation, l);
                if (lunar!=null) lunar.setRotation(rotation, l);
                if (flashred!=null) flashred.setRotation(rotation, l);
                if (flashyellow!=null) flashyellow.setRotation(rotation, l);
                if (flashgreen!=null) flashgreen.setRotation(rotation, l);
                if (flashlunar!=null) flashlunar.setRotation(rotation, l);
                if (dark!=null) dark.setRotation(rotation, l);
                if (held!=null) held.setRotation(rotation, l);
            }
        } catch (org.jdom.DataConversionException e) {}

        try {
            a = element.getAttribute("clickmode");
            if (a!=null) {
                l.setClickMode(a.getIntValue());
            }
        } catch (org.jdom.DataConversionException e) {
            log.error("Failed on clickmode attribute: "+e);
        }

        try {
            a = element.getAttribute("litmode");
            if (a!=null) {
                l.setLitMode(a.getBooleanValue());
            }
        } catch (org.jdom.DataConversionException e) {
            log.error("Failed on litmode attribute: "+e);
        }

        loadCommonAttributes(l, PanelEditor.SIGNALS.intValue(), element);

        NamedIcon icon = loadIcon( l,"red", element);
        if (icon!=null) { l.setRedIcon(icon); }

        icon = loadIcon( l,"yellow", element);
        if (icon!=null) { l.setYellowIcon(icon); }

        icon = loadIcon( l,"green", element);
        if (icon!=null) { l.setGreenIcon(icon); }

        icon = loadIcon( l,"lunar", element);
        if (icon!=null) { l.setLunarIcon(icon); }

        icon = loadIcon( l,"held", element);
        if (icon!=null) { l.setHeldIcon(icon); }

        icon = loadIcon( l,"dark", element);
        if (icon!=null) { l.setDarkIcon(icon); }

        icon = loadIcon( l,"flashred", element);
        if (icon!=null) { l.setFlashRedIcon(icon); }

        icon = loadIcon( l,"flashyellow", element);
        if (icon!=null) { l.setFlashYellowIcon(icon); }

        icon = loadIcon( l,"flashgreen", element);
        if (icon!=null) { l.setFlashGreenIcon(icon); }

        icon = loadIcon( l,"flashlunar", element);
        if (icon!=null) { l.setFlashLunarIcon(icon); }

        p.putLabel(l);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadIconXml.class.getName());

}
