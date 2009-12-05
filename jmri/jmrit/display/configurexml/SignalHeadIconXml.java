// SignalHeadIconXml.java

package jmri.jmrit.display.configurexml;

import jmri.SignalHead;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.SignalHeadIcon;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.SignalHeadIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.36 $
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
        
        element.setAttribute("signalhead", ""+p.getSignalHead().getSystemName());
        storeCommonAttributes(p, element);
        /*element.setAttribute("held", p.getHeldIcon().getURL());
        element.setAttribute("dark", p.getDarkIcon().getURL());
        element.setAttribute("red", p.getRedIcon().getURL());
        element.setAttribute("yellow", p.getYellowIcon().getURL());
        element.setAttribute("flashyellow", p.getFlashYellowIcon().getURL());
        element.setAttribute("green", p.getGreenIcon().getURL());
        element.setAttribute("lunar", p.getLunarIcon().getURL());
        element.setAttribute("flashred", p.getFlashRedIcon().getURL());
        element.setAttribute("flashgreen", p.getFlashGreenIcon().getURL());
        element.setAttribute("flashlunar", p.getFlashLunarIcon().getURL());
        element.setAttribute("rotate", String.valueOf(p.getGreenIcon().getRotation()));*/
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
        String className = o.getClass().getName();
		int lastDot = className.lastIndexOf(".");
		PanelEditor pe = null;
		LayoutEditor le = null;
        SignalHeadIcon l;
		String shortClass = className.substring(lastDot+1,className.length());
		if (shortClass.equals("PanelEditor")) {
			pe = (PanelEditor) o;
            l = new SignalHeadIcon();
            loadCommonAttributes(l, PanelEditor.SIGNALS.intValue(), element);
		}
		else if (shortClass.equals("LayoutEditor")) {
			le = (LayoutEditor) o;
            l = new SignalHeadIcon(le);
            loadCommonAttributes(l, LayoutEditor.SIGNALS.intValue(), element);
		}
		else {
			log.error("Unrecognizable class - "+className);
            l = new SignalHeadIcon();
		}

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
    
        int rotation = 0;
        try {
            attr = element.getAttribute("rotate");
            rotation = attr.getIntValue();
        } catch (org.jdom.DataConversionException e){
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }

        loadSignalIcon("red", rotation,l,element);
        loadSignalIcon("yellow", rotation,l,element);
        loadSignalIcon("green", rotation,l,element);
        loadSignalIcon("lunar", rotation,l,element);
        loadSignalIcon("held", rotation,l,element);
        loadSignalIcon("dark", rotation,l,element);
        loadSignalIcon("flashred", rotation,l,element);
        loadSignalIcon("flashyellow", rotation,l,element);
        loadSignalIcon("flashgreen", rotation,l,element);
        loadSignalIcon("flashlunar", rotation,l,element);
        
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

        
        if (pe!=null){
            pe.putLabel(l);
        }
        else if (le!=null){
            l.displayState(l.headState());
            le.putSignal(l);
        }
    }
    
    private void loadSignalIcon(String aspect, int rotation, SignalHeadIcon l, Element element){
        String name;
        NamedIcon icon = loadIcon( l,aspect, element);
        if (icon==null) {
            if (element.getAttribute(aspect) != null) {
                name = element.getAttribute(aspect).getValue();
                icon = NamedIcon.getIconByName(name);
                icon.setRotation(rotation, l);
            }
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
