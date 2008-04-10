package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.catalog.CatalogPane;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.PanelEditor;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.roster.RosterEntry;
import jmri.jmrit.roster.Roster;
import org.jdom.Attribute;
import org.jdom.Element;

/**
 * Handle configuration for display.LocoIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.2 $
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
        element.setAttribute("icon", icon.getName());
        RosterEntry entry = p.getRosterEntry();
        if (entry != null)
        	element.setAttribute("rosterentry", entry.getId());
        
        storeTextInfo(p, element);
        
        element.setAttribute("class", "jmri.jmrit.display.configurexml.LocoIconXml");

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
        String name = "error";

        LocoIcon l = new LocoIcon();
        
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
        int level = PanelEditor.MARKERS.intValue();
        try {
            level = element.getAttribute("level").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.warn("Could not parse level attribute!");
        } catch ( NullPointerException e) {  // considered normal if the attribute not present
        }
        l.setDisplayLevel(level);
        
         try {
			name = element.getAttribute("icon").getValue();
			l.setIcon(CatalogPane.getIconByName(name));
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
        
        p.putLocoIcon(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIconXml.class.getName());
}