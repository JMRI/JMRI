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
 * @version $Revision: 1.44 $
 */
public class SignalHeadIconXml extends PositionableLabelXml {

    static final java.util.ResourceBundle rbean = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle");

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

        NamedIcon icon = p.getIcon(rbean.getString("SignalHeadStateHeld"));
        if (icon!=null) {
            element.addContent(storeIcon("held", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateDark"));
        if (icon!=null) {
            element.addContent(storeIcon("dark", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateRed"));
        if (icon!=null) {
            element.addContent(storeIcon("red", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateYellow"));
        if (icon!=null) {
            element.addContent(storeIcon("yellow", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateGreen"));
        if (icon!=null) {
            element.addContent(storeIcon("green", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateFlashingYellow"));
        if (icon!=null) {
            element.addContent(storeIcon("flashyellow", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateLunar"));
        if (icon!=null) {
            element.addContent(storeIcon("lunar", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateFlashingRed"));
        if (icon!=null) {
            element.addContent(storeIcon("flashred", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateFlashingGreen"));
        if (icon!=null) {
            element.addContent(storeIcon("flashgreen", icon));
        }
        icon = p.getIcon(rbean.getString("SignalHeadStateFlashingLunar"));
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
            if (aspect.equals("red")) l.setIcon(rbean.getString("SignalHeadStateRed"), icon);
            else if (aspect.equals("yellow")) l.setIcon(rbean.getString("SignalHeadStateYellow"), icon);
            else if (aspect.equals("green")) l.setIcon(rbean.getString("SignalHeadStateGreen"), icon);
            else if (aspect.equals("lunar")) l.setIcon(rbean.getString("SignalHeadStateLunar"), icon);
            else if (aspect.equals("held")) l.setIcon(rbean.getString("SignalHeadStateHeld"), icon);
            else if (aspect.equals("dark")) l.setIcon(rbean.getString("SignalHeadStateDark"), icon);
            else if (aspect.equals("flashred")) l.setIcon(rbean.getString("SignalHeadStateFlashingRed"), icon);
            else if (aspect.equals("flashyellow")) l.setIcon(rbean.getString("SignalHeadStateFlashingYellow"), icon);
            else if (aspect.equals("flashgreen")) l.setIcon(rbean.getString("SignalHeadStateFlashingGreen"), icon);
            else if (aspect.equals("flashlunar")) l.setIcon(rbean.getString("SignalHeadStateFlashingLunar"), icon);
        }
    
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadIconXml.class.getName());
}
