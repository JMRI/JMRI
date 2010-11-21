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
 * @version $Revision: 1.34 $
 */
public class TurnoutIconXml extends PositionableLabelXml {

    static final HashMap<String,String> _nameMap = new HashMap<String,String>();

    public TurnoutIconXml() {
        // map previous store names to actual localized names
        _nameMap.put("closed", "TurnoutStateClosed");
        _nameMap.put("thrown", "TurnoutStateThrown");
        _nameMap.put("unknown", "BeanStateUnknown");
        _nameMap.put("inconsistent", "BeanStateInconsistent");
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
        Element elem = new Element("icons");
        elem.addContent(storeIcon("closed", p.getIcon("TurnoutStateClosed")));
        elem.addContent(storeIcon("thrown", p.getIcon("TurnoutStateThrown")));
        elem.addContent(storeIcon("unknown", p.getIcon("BeanStateUnknown")));
        elem.addContent(storeIcon("inconsistent", p.getIcon("BeanStateInconsistent")));
        element.addContent(elem);

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
        
        Attribute a = element.getAttribute("tristate");
        if ( (a==null) || ((a!=null) && a.getValue().equals("true")))
            l.setTristate(true);
        else
            l.setTristate(false);

        @SuppressWarnings("unchecked")
        List<Element>states = element.getChildren();
        if (states.size()>0) {
            if (log.isDebugEnabled()) log.debug("Main element has"+states.size()+" items");
            Element elem = element;     // the element containing the icons
            Element icons = element.getChild("icons");
            if (icons!=null) {
                @SuppressWarnings("unchecked")
                List<Element>s = icons.getChildren();
                states = s;
                elem = icons;          // the element containing the icons
                if (log.isDebugEnabled()) log.debug("icons element has"+states.size()+" items");
            }
            for (int i=0; i<states.size(); i++) {
                String state = states.get(i).getName();
                if (log.isDebugEnabled()) log.debug("setIcon for state \""+state+
                                                    "\" and "+_nameMap.get(state));
                NamedIcon icon = loadIcon(l, state, elem);
                l.setIcon(_nameMap.get(state), icon);
            }
            log.debug(states.size()+" icons loaded for "+l.getNameString());
        }
            
        p.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.TURNOUTS, element);
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TurnoutIconXml.class.getName());
}