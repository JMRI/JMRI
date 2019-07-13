package jmri.jmrit.display.configurexml;

import java.util.List;
import jmri.Memory;
import jmri.jmrit.catalog.NamedIcon;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.MemoryIcon;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.MemoryIcon objects.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2004
 */
public class MemoryIconXml extends PositionableLabelXml {

    public MemoryIconXml() {
    }

    /**
     * Default implementation for storing the contents of a MemoryIcon
     *
     * @param o Object to store, of type MemoryIcon
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        MemoryIcon p = (MemoryIcon) o;

        Element element = new Element("memoryicon");

        // include attributes
        element.setAttribute("memory", p.getNamedMemory().getName());
        storeCommonAttributes(p, element);
        storeTextInfo(p, element);

        //If the fixed width option is not set and the justification is not left
        //Then we need to replace the x, y values with the original ones.
        if (p.getPopupUtility().getFixedWidth() == 0 && p.getPopupUtility().getJustification() != 0) {
            element.setAttribute("x", "" + p.getOriginalX());
            element.setAttribute("y", "" + p.getOriginalY());
        }
        element.setAttribute("selectable", (p.isSelectable() ? "yes" : "no"));
        if (p.updateBlockValueOnChange()) {
            element.setAttribute("updateBlockValue", (p.updateBlockValueOnChange() ? "yes" : "no"));
        }

        element.setAttribute("class", "jmri.jmrit.display.configurexml.MemoryIconXml");
        if (p.getDefaultIcon() != null) {
            element.setAttribute("defaulticon", p.getDefaultIcon().getURL());
        }

        // include contents
        java.util.HashMap<String, NamedIcon> map = p.getMap();
        if (map != null) {

            java.util.Iterator<java.util.Map.Entry<String, NamedIcon>> iterator = map.entrySet().iterator();

            while (iterator.hasNext()) {
                java.util.Map.Entry<String, NamedIcon> mi = iterator.next();
                String key = mi.getKey();
                String value = mi.getValue().getName();

                Element e2 = new Element("memorystate");
                e2.setAttribute("value", key);
                e2.setAttribute("icon", value);
                element.addContent(e2);
            }
        }
        return element;
    }

    /**
     * Load, starting with the memoryicon element, then all the value-icon pairs
     *
     * @param element Top level Element to unpack.
     * @param o       an Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {

        Editor ed = null;
        MemoryIcon l;
        if (o instanceof LayoutEditor) {
            ed = (LayoutEditor) o;
            l = new jmri.jmrit.display.layoutEditor.MemoryIcon("   ", (LayoutEditor) ed);
        } else if (o instanceof jmri.jmrit.display.Editor) {
            ed = (Editor) o;
            l = new MemoryIcon("", ed);
        } else {
            log.error("Unrecognizable class - " + o.getClass().getName());
            return;
        }

        String name;
        Attribute attr = element.getAttribute("memory");
        if (attr == null) {
            log.error("incorrect information for a memory location; must use memory name");
            ed.loadFailed();
            return;
        } else {
            name = attr.getValue();
        }

        loadTextInfo(l, element);

        Memory m = jmri.InstanceManager.memoryManagerInstance().getMemory(name);
        if (m != null) {
            l.setMemory(name);
        } else {
            log.error("Memory named '" + attr.getValue() + "' not found.");
            ed.loadFailed();
        }

        Attribute a = element.getAttribute("selectable");
        if (a != null && a.getValue().equals("yes")) {
            l.setSelectable(true);
        } else {
            l.setSelectable(false);
        }

        a = element.getAttribute("defaulticon");
        if (a != null) {
            l.setDefaultIcon(NamedIcon.getIconByName(a.getValue()));
        }
        
        a = element.getAttribute("updateBlockValue");
        if (a != null && a.getValue().equals("yes")) {
            l.updateBlockValueOnChange(true);
        }

        // get the icon pairs
        List<Element> items = element.getChildren("memorystate");
        for (int i = 0; i < items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = items.get(i);
            String iconName = item.getAttribute("icon").getValue();
            NamedIcon icon = NamedIcon.getIconByName(iconName);
            if (icon == null) {
                icon = ed.loadFailed("Memory " + name, iconName);
                if (icon == null) {
                    log.info("Memory \"" + name + "\" icon removed for url= " + iconName);
                }
            }
            if (icon != null) {
                String keyValue = item.getAttribute("value").getValue();
                l.addKeyAndIcon(icon, keyValue);
            }
        }
        ed.putItem(l);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(l, Editor.MEMORIES, element);
        int x = 0;
        int y = 0;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert positional attribute");
        }
        l.setOriginalLocation(x, y);
        l.displayState();
    }

    private final static Logger log = LoggerFactory.getLogger(MemoryIconXml.class);
}
