package jmri.jmrit.display.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.LocoIcon;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.LocoIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 */
public class LocoIconXml extends PositionableLabelXml {

    public LocoIconXml() {
    }

    /**
     * Default implementation for storing the contents of a LocoIcon
     *
     * @param o Object to store, of type LocoIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        LocoIcon p = (LocoIcon) o;
        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("locoicon");
        storeCommonAttributes(p, element);

        // include contents
        if (p.getUnRotatedText() != null) {
            element.setAttribute("text", p.getUnRotatedText());
        }
        storeTextInfo(p, element);
        element.setAttribute("icon", "yes");
        element.setAttribute("dockX", "" + p.getDockX());
        element.setAttribute("dockY", "" + p.getDockY());
        element.addContent(storeIcon("icon", (NamedIcon) p.getIcon()));
        RosterEntry entry = p.getRosterEntry();
        if (entry != null) {
            element.setAttribute("rosterentry", entry.getId());
        }
        element.setAttribute("class", "jmri.jmrit.display.configurexml.LocoIconXml");

        return element;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        Editor ed = (Editor) o;
        LocoIcon l = new LocoIcon(ed);

        // create the objects
        String textName = "error";
        try {
            textName = element.getAttribute("text").getValue();
        } catch (Exception e) {
            log.error("failed to get loco text attribute ex= " + e);
        }
        String name = "error";
        NamedIcon icon;
        try {
            name = element.getAttribute("icon").getValue();
        } catch (Exception e) {
            log.error("failed to get icon attribute ex= " + e);
        }
        if (name.equals("yes")) {
            icon = loadIcon(l, "icon", element, "LocoIcon", ed);
        } else {
            icon = NamedIcon.getIconByName(name);
            if (icon == null) {
                icon = ed.loadFailed("LocoIcon", name);
                if (icon == null) {
                    log.info("LocoIcon icon removed for url= " + name);
                    return;
                }
            }
        }
        l.updateIcon(icon);

        try {
            int x = element.getAttribute("dockX").getIntValue();
            int y = element.getAttribute("dockY").getIntValue();
            l.setDockingLocation(x, y);
            //           l.dock();
        } catch (Exception e) {
            log.warn("failed to get docking location= " + e);
        }

        String rosterId = null;
        try {
            rosterId = element.getAttribute("rosterentry").getValue();
            RosterEntry entry = Roster.getDefault().entryFromTitle(rosterId);
            l.setRosterEntry(entry);
        } catch (Exception e) {
            log.debug("no roster entry for " + rosterId + ", ex= " + e);
        }
        ed.putLocoIcon(l, textName);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.MARKERS, element);
        loadTextInfo(l, element);

        l.init();  // to detect "background" color for use in Tracker, examine icon file 
    }

    private final static Logger log = LoggerFactory.getLogger(LocoIconXml.class);
}
