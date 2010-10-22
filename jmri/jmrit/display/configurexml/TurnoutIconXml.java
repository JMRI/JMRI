package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.TurnoutIcon;
import org.jdom.Attribute;
import org.jdom.Element;
import java.util.List;
import java.util.HashMap;

/**
 * Handle configuration for display.TurnoutIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.32 $
 */
public class TurnoutIconXml extends PositionableLabelXml {

    static final java.util.ResourceBundle rbean = java.util.ResourceBundle.getBundle("jmri.NamedBeanBundle");
    static final HashMap<String,String> _nameMap = new HashMap<String,String>();

    public TurnoutIconXml() {
        // map previous store names to actual localized names
        _nameMap.put("closed", rbean.getString("TurnoutStateClosed"));
        _nameMap.put("thrown", rbean.getString("TurnoutStateThrown"));
        _nameMap.put("unknown", rbean.getString("BeanStateUnknown"));
        _nameMap.put("inconsistent", rbean.getString("BeanStateInconsistent"));
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
        element.addContent(storeIcon("closed", p.getIcon(rbean.getString("TurnoutStateClosed"))));
        element.addContent(storeIcon("thrown", p.getIcon(rbean.getString("TurnoutStateThrown"))));
        element.addContent(storeIcon("unknown", p.getIcon(rbean.getString("BeanStateUnknown"))));
        element.addContent(storeIcon("inconsistent", p.getIcon(rbean.getString("BeanStateInconsistent"))));

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
     * @param o  Editor as an Object
     */
    @SuppressWarnings("null")
	public void load(Element element, Object o) {
        // create the objects
        Editor p = (Editor)o;

        TurnoutIcon l = new TurnoutIcon(p);
        
        String name;
        try {
            name=element.getAttribute("turnout").getValue();
        } catch ( NullPointerException e) { 
            log.error("incorrect information for turnout; must use turnout name");
            return;
        }
        l.setTurnout(name);
        
        @SuppressWarnings("unchecked")
        List<Element>states = element.getChildren();
        if (states.size()>0) {
            for (int i=0; i<states.size(); i++) {
                String state = states.get(i).getName();
                NamedIcon icon = loadIcon(l, state, element);
                l.setIcon(_nameMap.get(state), icon);
            }
            log.debug(states.size()+" icons loaded for "+l.getNameString());
        }
        Attribute a = element.getAttribute("tristate");
        if ( (a==null) || ((a!=null) && a.getValue().equals("true")))
            l.setTristate(true);
        else
            l.setTristate(false);
            
        p.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.TURNOUTS, element);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TurnoutIconXml.class.getName());
}