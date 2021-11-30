package jmri.jmrit.display.controlPanelEditor.configurexml;

import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.configurexml.PositionableLabelXml;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.display.controlPanelEditor.PortalIcon;
import jmri.jmrit.display.palette.ItemPalette;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.Portal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.display.Positionable;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.PortalIcon objects.
 *
 * @author Pete cressman Copyright: Copyright (c) 2010, 2020
 */
public class PortalIconXml extends PositionableLabelXml {

    public PortalIconXml() {
    }

    /**
     * Default implementation for storing the contents of a PortalIcon.
     *
     * @param o Object to store, of type PortalIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        PortalIcon p = (PortalIcon) o;
        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("PortalIcon");
        storeCommonAttributes(p, element);
        element.setAttribute("scale", String.valueOf(p.getScale()));
        element.setAttribute("rotate", String.valueOf(p.getDegrees()));

        // include contents
        Portal portal = p.getPortal();
        if (portal == null) {
            log.info("PortalIcon has no associated Portal.");
            return null;
        }
        element.setAttribute("portalName", portal.getName());
        if (portal.getToBlock() != null) {
            element.setAttribute("toBlockName", portal.getToBlockName());
        }
        if (portal.getFromBlockName() != null) {
            element.setAttribute("fromBlockName", portal.getFromBlockName());
        }
        element.setAttribute("arrowSwitch", "" + (p.getArrowSwitch() ? "yes" : "no"));
        element.setAttribute("arrowHide", "" + (p.getArrowHide() ? "yes" : "no"));

        HashMap<String, NamedIcon> map = p.getIconMap();
        if (map != null) {
            Element elem = new Element("icons");
            String family = p.getFamily();
            if (family != null) {
                elem.setAttribute("family", family);
            }
            for (Map.Entry<String, NamedIcon> entry : map.entrySet()) {
                elem.addContent(storeIcon(entry.getKey(), entry.getValue()));
            }
        }

        element.setAttribute("class", "jmri.jmrit.display.controlPanelEditor.configurexml.PortalIconXml");
        return element;
    }

    /**
     * Create a PositionableLabel, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML
     */
    @Override
    public void load(Element element, Object o) throws JmriConfigureXmlException {
        if (!(o instanceof ControlPanelEditor)) {
            log.error("Can't load portalIcon.  Panel editor must use ControlPanelEditor.");
            return;
        }
        ControlPanelEditor ed = (ControlPanelEditor) o;

        String fromBlk;
        try {
            fromBlk = element.getAttribute("fromBlockName").getValue();
        } catch (NullPointerException e) {
            log.error("incorrect information for portalIcon; must use fromBlockName.");
            return;
        }
        String portalName;
        try {
            portalName = element.getAttribute("portalName").getValue();
        } catch (NullPointerException e) {
            log.error("incorrect information for portalIcon; must use portalName.");
            return;
        }
        OBlock block = jmri.InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class).getOBlock(fromBlk);
        Portal portal = block.getPortalByName(portalName);

        PortalIcon l = new PortalIcon(ed, portal);
        try {
            ed.putItem(l);
        } catch (Positionable.DuplicateIdException e) {
            throw new JmriConfigureXmlException("Positionable id is not unique", e);
        }
        
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, ControlPanelEditor.MARKERS, element);
        Attribute a = element.getAttribute("scale");
        double scale = 1.0;
        if (a != null) {
            try {
                scale = a.getDoubleValue();
            } catch (org.jdom2.DataConversionException dce) {
                log.error("{} can't convert scale {}", l.getNameString(), dce);
            }
        }
        l.setScale(scale);

        a = element.getAttribute("rotate");
        int deg = 0;
        if (a != null) {
            try {
                deg = a.getIntValue();
            } catch (org.jdom2.DataConversionException dce) {
                log.error("{} can't convert rotate {}", l.getNameString(), dce);
            }
        }
        l.setDegrees(deg);

        boolean value = true;
        if ((a = element.getAttribute("arrowSwitch")) != null && a.getValue().equals("no")) {
            value = false;
        }
        l.setArrowOrientation(value);
        value = (a = element.getAttribute("arrowHide")) != null && a.getValue().equals("yes");
        l.setHideArrows(value);

        Element iconsElem = element.getChild("icons");
        if (iconsElem != null) {
            Attribute attr = iconsElem.getAttribute("family");
            if (attr != null) {
                l.setFamily(attr.getValue());
            }
            List<Element> states = iconsElem.getChildren();
            if (log.isDebugEnabled()) {
                log.debug("icons element has{} items", states.size());
            }
            for (Element stateElem : states) {
                String state = stateElem.getName();
                if (log.isDebugEnabled()) {
                    log.debug("setIcon for state \"{}\"", state);
                }
                NamedIcon icon = loadIcon(l, state, iconsElem, "PortalIcon \"" + portalName + "\": icon \"" + state + "\" ", ed);
                if (icon != null) {
                    l.setIcon(state, icon);
                } else {
                    log.info("PortalIcon \"{}\": icon \"{}\" removed", portalName, state);
                    return;
                }
            }
        } else {
            ItemPalette.loadIcons();
            l.makeIconMap();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PortalIconXml.class);
}
