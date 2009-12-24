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
 * @version $Revision: 1.28 $
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
        element.setAttribute("turnout", p.getNamedTurnout().getName());
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
        
        TurnoutIcon l = new TurnoutIcon();

        int rotation = 0;
        try {
            Attribute a = element.getAttribute("rotate");
            rotation = a.getIntValue();
        } catch (org.jdom.DataConversionException e) {
        } catch ( NullPointerException e) {  // considered normal if the attributes are not present
        }
        
        String name;
        try {
            name=element.getAttribute("turnout").getValue();
        } catch ( NullPointerException e) { 
            log.error("incorrect information for turnout; must use turnout name");
            return;
        }

        loadCommonAttributes(l, PanelEditor.TURNOUTS.intValue(), element);

        loadTurnoutIcon("closed", rotation, l, element, name);
        loadTurnoutIcon("thrown", rotation, l, element, name);
        loadTurnoutIcon("unknown", rotation, l, element, name);
        loadTurnoutIcon("inconsistent", rotation, l, element, name);
        
        Attribute a = element.getAttribute("tristate");
        if ( (a==null) || ((a!=null) && a.getValue().equals("true")))
            l.setTristate(true);
        else
            l.setTristate(false);
            
        l.setTurnout(name);
        
        p.putLabel(l);
    }
    
    private void loadTurnoutIcon(String state, int rotation, TurnoutIcon l, Element element, String name){
        NamedIcon icon = loadIcon( l,state, element);
        if (icon==null){
            if (element.getAttribute(state) != null) {
                String iconName;
                iconName = element.getAttribute(state).getValue();
                icon = NamedIcon.getIconByName(iconName);
                icon.setRotation(rotation, l);
            }
            else log.warn("did not locate " + state + " icon file "+name);
        }
        if (icon!=null) {
            if(state.equals("closed")) l.setClosedIcon(icon);
            else if (state.equals("thrown")) l.setThrownIcon(icon);
            else if (state.equals("unknown")) l.setUnknownIcon(icon);
            else if (state.equals("inconsistent")) l.setInconsistentIcon(icon);
        }
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TurnoutIconXml.class.getName());
}