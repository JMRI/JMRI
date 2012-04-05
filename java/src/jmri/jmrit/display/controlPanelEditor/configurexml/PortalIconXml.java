package jmri.jmrit.display.controlPanelEditor.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.display.controlPanelEditor.PortalIcon;
import jmri.jmrit.display.configurexml.PositionableLabelXml;
import jmri.jmrit.logix.Portal;

import java.util.List;
import org.jdom.Element;

/**
 * Handle configuration for display.PortalIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision$
 */
public class PortalIconXml extends PositionableLabelXml {

    public PortalIconXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * PortalIcon
     * @param o Object to store, of type PortalIcon
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        PortalIcon p = (PortalIcon)o;
        if (!p.isActive()) return null;  // if flagged as inactive, don't store

        Element element = new Element("PortalIcon");
        storeCommonAttributes(p, element);

        // include contents
        Portal portal = p.getPortal();
        element.setAttribute("portalName", portal.getName());
        if (portal.getToBlock()!=null) { 
            element.setAttribute("toBlockName", portal.getToBlockName());
        }
        element.setAttribute("fromBlockName", portal.getFromBlockName());

        Element elem = new Element("icons");
        NamedIcon icon = p.getIcon(PortalIcon.HIDDEN);
        if (icon!=null) {
            elem.addContent(storeIcon("hidden", icon));
        }
        icon = p.getIcon(PortalIcon.BLOCK);
        if (icon!=null) {
            elem.addContent(storeIcon("block", icon));
        }
        icon = p.getIcon(PortalIcon.PATH);
        if (icon!=null) {
            elem.addContent(storeIcon("path", icon));
        }
        element.addContent(elem);

        element.setAttribute("class", "jmri.jmrit.display.controlPanelEditor.configurexml.PortalIconXml");
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
    	if (!(o instanceof ControlPanelEditor)) {
            log.error("Can't load portalIcon.  Panel editor must use ControlPanelEditor.");
            return;   		
    	}
    	ControlPanelEditor ed = (ControlPanelEditor) o;
        
        String fromBlk;
        try {
            fromBlk=element.getAttribute("fromBlockName").getValue();
        } catch ( NullPointerException e) { 
            log.error("incorrect information for portalIcon; must use fromBlockName.");
            ed.loadFailed();
            return;
        }
        String portalName;
        try {
            portalName=element.getAttribute("portalName").getValue();
        } catch ( NullPointerException e) { 
            log.error("incorrect information for portalIcon; must use portalName.");
            ed.loadFailed();
            return;
        }
        PortalIcon l= new PortalIcon(fromBlk, portalName, ed);

        try {
            Element icons = element.getChild("icons");
            @SuppressWarnings("unchecked")
            List<Element> iconList = icons.getChildren();
            for (int i=0; i<iconList.size(); i++) {
                Element iconElem = iconList.get(i);
                String name = iconElem.getName();
                NamedIcon icon = loadIcon(l, name, icons, "PortalIcon \""+portalName+"\": icon \""+name+"\" ", ed);
                if (icon!=null) {
                    l.setIcon(name, icon);
                } else {
                    log.info("PortalIcon \""+portalName+"\": icon \""+name+"\" removed");
                }
            }
        } catch ( NullPointerException e) { 
            log.error("incorrect information for portalIcon; missing icons.");
            ed.loadFailed();
            return;
        }
    	
        ed.putPortalIcon(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, ControlPanelEditor.MARKERS, element);        
     }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(PortalIconXml.class.getName());
}
