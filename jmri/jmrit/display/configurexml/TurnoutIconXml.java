package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.TurnoutIcon;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.TurnoutIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.27 $
 */
public class TurnoutIconXml extends PositionableLabelXml {

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
        element.setAttribute("turnout", p.getTurnout().getSystemName());
        storeCommonAttributes(p, element);

        // old style 
        //element.setAttribute("closed", p.getClosedIcon().getURL());
        //element.setAttribute("thrown", p.getThrownIcon().getURL());
        //element.setAttribute("unknown", p.getUnknownIcon().getURL());
        //element.setAttribute("inconsistent", p.getInconsistentIcon().getURL());
        // element.setAttribute("rotate", String.valueOf(p.getClosedIcon().getRotation()));
        // include contents
        element.setAttribute("tristate", p.getTristate()?"true":"false");
        
        // new style
        element.addContent(storeIcon("closed", p.getClosedIcon()));
        element.addContent(storeIcon("thrown", p.getThrownIcon()));
        element.addContent(storeIcon("unknown", p.getUnknownIcon()));
        element.addContent(storeIcon("inconsistent", p.getInconsistentIcon()));

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
        String name = "";
        NamedIcon closed = null;
        NamedIcon thrown = null;
        NamedIcon unknown = null;
        NamedIcon inconsistent = null;

        TurnoutIcon l = new TurnoutIcon();

        // load old style icons      
        if (element.getAttribute("closed") != null)
        	l.setClosedIcon(closed = NamedIcon.getIconByName(element.getAttribute("closed").getValue()));
        if (element.getAttribute("thrown") != null)
        	l.setThrownIcon(thrown = NamedIcon.getIconByName(element.getAttribute("thrown").getValue()));      
        if (element.getAttribute("unknown") != null)
        	l.setUnknownIcon(unknown = NamedIcon.getIconByName(element.getAttribute("unknown").getValue()));
        if (element.getAttribute("inconsistent") != null){
        	l.setInconsistentIcon(inconsistent = NamedIcon.getIconByName(element.getAttribute("inconsistent").getValue()));
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
        }

        loadCommonAttributes(l, PanelEditor.TURNOUTS.intValue(), element);

        Attribute a = element.getAttribute("tristate");
        if ( (a==null) || ((a!=null) && a.getValue().equals("true")))
            l.setTristate(true);
        else
            l.setTristate(false);
            
        l.setTurnout(name = element.getAttribute("turnout").getValue());
        
        // load new style icons
        NamedIcon icon = loadIcon( l,"closed", element);
        if (icon!=null) {
        	closed = icon;
        	l.setClosedIcon(icon); 
        }
        icon = loadIcon( l,"thrown", element);
        if (icon!=null) {
        	thrown = icon;
        	l.setThrownIcon(icon); 
        }
        icon = loadIcon( l,"unknown", element);
        if (icon!=null) {
        	unknown = icon;
        	l.setUnknownIcon(icon); 
        }
        icon = loadIcon( l,"inconsistent", element);
        if (icon!=null) {
        	inconsistent = icon;
        	l.setInconsistentIcon(icon); 
        }       
        // report errors
        if (closed == null) log.warn("did not locate closed icon turnout "+name);
        if (thrown == null) log.warn("did not locate thrown icon turnout "+name);
        if (unknown == null) log.warn("did not locate unknown icon turnout "+name);
        if (inconsistent == null) log.warn("did not locate inconsistent icon turnout "+name);
        
        p.putLabel(l);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TurnoutIconXml.class.getName());
}