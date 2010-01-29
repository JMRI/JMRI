package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.Roster;
import org.jdom.Element;

/**
 * Handle configuration for display.LocoIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.12 $
 */
public class LocoIconXml extends PositionableLabelXml {

    public LocoIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LocoIcon
     * @param o Object to store, of type LocoIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        LocoIcon p = (LocoIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("locoicon");

        // include contents
        element.setAttribute("text", p.getText());
        element.setAttribute("x", ""+p.getX());
        element.setAttribute("y", ""+p.getY());
        element.setAttribute("level", String.valueOf(p.getDisplayLevel()));
        NamedIcon icon = (NamedIcon)p.getIcon();
        element.setAttribute("icon", icon.getURL());
        RosterEntry entry = p.getRosterEntry();
        if (entry != null)
        	element.setAttribute("rosterentry", entry.getId());
        
        storeTextInfo(p, element);
        
        element.setAttribute("class", "jmri.jmrit.display.configurexml.LocoIconXml");

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
        LocoIcon l;
 		// get object class and determine editor being used
		String className = o.getClass().getName();
		int lastDot = className.lastIndexOf(".");
		PanelEditor pe = null;
		LayoutEditor le = null;
		String shortClass = className.substring(lastDot+1,className.length());
		if (shortClass.equals("PanelEditor")) {
			pe = (PanelEditor) o;
            l = new LocoIcon(pe);
            pe.putItem(l);
		}
		else if (shortClass.equals("LayoutEditor")) {
			le = (LayoutEditor) o;
            l = new LocoIcon(le);
            le.putItem(l);
		}
		else {
			log.error("Unrecognizable class - "+className);
            return;
		}
       // create the objects
        String name = "error";
        
        try {
            name = element.getAttribute("text").getValue();
         } catch ( Exception e) {
            log.error("failed to get loco text attribute");
        }
        l.setText (name);
        loadTextInfo(l, element);
        
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
		int level =  Editor.MARKERS;
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);
        
         try {
			name = element.getAttribute("icon").getValue();
			l.setIcon(NamedIcon.getIconByName(name));
		} catch (Exception e) {
			log.error("failed to get icon attribute");
		}
		
		try{
			String rosterId = element.getAttribute("rosterentry").getValue();
			RosterEntry entry = Roster.instance().entryFromTitle(rosterId);
			l.setRosterEntry(entry);
		} catch (Exception e) {
			log.debug("no roster entry");
		}
     }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LocoIconXml.class.getName());
}