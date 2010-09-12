// SignalHeadIconXml.java

package jmri.jmrit.display.configurexml;

import jmri.SignalHead;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.SignalHeadIcon;
import jmri.util.NamedBeanHandle;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.SignalHeadIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.43 $
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
        
        element.setAttribute("signalhead", ""+p.getNameString());
        storeCommonAttributes(p, element);
        element.setAttribute("clickmode", ""+p.getClickMode());
        element.setAttribute("litmode", ""+p.getLitMode());

        NamedIcon icon = p.getHeldIcon();
        if (icon!=null) {
            element.addContent(storeIcon("held", icon));
        }
        icon = p.getDarkIcon();
        if (icon!=null) {
            element.addContent(storeIcon("dark", icon));
        }
        icon = p.getRedIcon();
        if (icon!=null) {
            element.addContent(storeIcon("red", icon));
        }
        icon = p.getYellowIcon();
        if (icon!=null) {
            element.addContent(storeIcon("yellow", icon));
        }
        icon = p.getGreenIcon();
        if (icon!=null) {
            element.addContent(storeIcon("green", icon));
        }
        icon = p.getFlashYellowIcon();
        if (icon!=null) {
            element.addContent(storeIcon("flashyellow", icon));
        }
        icon = p.getLunarIcon();
        if (icon!=null) {
            element.addContent(storeIcon("lunar", icon));
        }
        icon = p.getFlashRedIcon();
        if (icon!=null) {
            element.addContent(storeIcon("flashred", icon));
        }
        icon = p.getFlashGreenIcon();
        if (icon!=null) {
            element.addContent(storeIcon("flashgreen", icon));
        }
        icon = p.getFlashLunarIcon();
        if (icon!=null) {
            element.addContent(storeIcon("flashlunar", icon));
        }

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
     * @param o  an Editor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        Editor ed = (Editor)o;
        SignalHeadIcon l = new SignalHeadIcon(ed);
        String name;

        Attribute attr = element.getAttribute("signalhead"); 
        if (attr == null) {
            log.error("incorrect information for signal head; must use signalhead name");
            return;
        } else {
            name = attr.getValue();
        }
        
        SignalHead sh = jmri.InstanceManager.signalHeadManagerInstance().getSignalHead(name);

        if (sh != null) {
            l.setSignalHead(new NamedBeanHandle<SignalHead>(name, sh));
        } else {
            log.error("SignalHead named '"+attr.getValue()+"' not found.");
            return;
        }
        int rotation = 0;
        try {
            attr = element.getAttribute("rotate");
            rotation = attr.getIntValue();
        } catch (org.jdom.DataConversionException e){
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }

        loadSignalIcon("red", rotation,l,element, name);
        loadSignalIcon("yellow", rotation,l,element, name);
        loadSignalIcon("green", rotation,l,element, name);
        loadSignalIcon("lunar", rotation,l,element, name);
        loadSignalIcon("held", rotation,l,element, name);
        loadSignalIcon("dark", rotation,l,element, name);
        loadSignalIcon("flashred", rotation,l,element, name);
        loadSignalIcon("flashyellow", rotation,l,element, name);
        loadSignalIcon("flashgreen", rotation,l,element, name);
        loadSignalIcon("flashlunar", rotation,l,element, name);
        
        try {
            attr = element.getAttribute("clickmode");
            if (attr!=null) {
                l.setClickMode(attr.getIntValue());
            }
        } catch (org.jdom.DataConversionException e) {
            log.error("Failed on clickmode attribute: "+e);
        }

        try {
            attr = element.getAttribute("litmode");
            if (attr!=null) {
                l.setLitMode(attr.getBooleanValue());
            }
        } catch (org.jdom.DataConversionException e) {
            log.error("Failed on litmode attribute: "+e);
        }

        ed.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.SIGNALS, element);
    }
    
    private void loadSignalIcon(String aspect, int rotation, SignalHeadIcon l, Element element, String name){
        NamedIcon icon = loadIcon( l,aspect, element);
        if (icon==null) {
            if (element.getAttribute(aspect) != null) {
            String iconName;
                iconName = element.getAttribute(aspect).getValue();
                icon = NamedIcon.getIconByName(iconName);
                icon.setRotation(rotation, l);
            }
            else log.warn("did not locate " + aspect + " icon file "+name);
        }
        if (icon!=null){
            if (aspect.equals("red")) l.setRedIcon(icon);
            else if (aspect.equals("yellow")) l.setYellowIcon(icon);
            else if (aspect.equals("green")) l.setGreenIcon(icon);
            else if (aspect.equals("lunar")) l.setLunarIcon(icon);
            else if (aspect.equals("held")) l.setHeldIcon(icon);
            else if (aspect.equals("dark")) l.setDarkIcon(icon);
            else if (aspect.equals("flashred")) l.setFlashRedIcon(icon);
            else if (aspect.equals("flashyellow")) l.setFlashYellowIcon(icon);
            else if (aspect.equals("flashgreen")) l.setFlashGreenIcon(icon);
            else if (aspect.equals("flashlunar")) l.setFlashLunarIcon(icon);
        }
    
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadIconXml.class.getName());
}
