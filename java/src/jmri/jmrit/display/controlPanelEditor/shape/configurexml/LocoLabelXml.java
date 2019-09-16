package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.InstanceManager;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.shape.LocoLabel;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import org.jdom2.Element;

/**
 * Handle configuration for display.PositionableShape objects
 *
 * @author Pete Cressman Copyright (c) 2012
 */
@Deprecated     // Very unlikely to have been used for 2+ years or more. Usefulness doubtful.  
public class LocoLabelXml extends PositionableRectangleXml {

    public LocoLabelXml() {
    }

    /**
     * Default implementation for storing the contents of a PositionableShape
     *
     * @param o Object to store, of type PositionableShape
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        LocoLabel p = (LocoLabel) o;

        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("LocoLabel");
        storeCommonAttributes(p, element);

        Element elem = new Element("size");
        elem.setAttribute("width", "" + p.getWidth());
        elem.setAttribute("height", "" + p.getHeight());
        element.addContent(elem);

        elem = new Element("OBlock");
        OBlock block = p.getBlock();
        elem.setAttribute("systemName", "" + block.getSystemName());
        elem.setAttribute("trainName", "" + block.getValue());
        element.addContent(elem);

        element.setAttribute("class", "jmri.jmrit.display.controlPanelEditor.shape.configurexml.LocoLabelXml");
        return element;
    }

    /**
     * Create a PositionableShape, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        Editor ed = (Editor) o;
        LocoLabel ll = new LocoLabel(ed);

        Element elem = element.getChild("size");
        ll.setWidth(getInt(elem, "width"));
        ll.setHeight(getInt(elem, "height"));

        if (elem != null && elem.getAttribute("systemName") != null) {
            String name = elem.getAttribute("systemName").getValue();
            OBlockManager manager = InstanceManager.getDefault(jmri.jmrit.logix.OBlockManager.class);
            OBlock block = manager.getBySystemName(name);
            ll.setBlock(block);
            if (elem.getAttribute("trainName") != null && block != null) {
                block.setValue(elem.getAttribute("trainName").getValue());
            }
        } else {
            return;     // don't put into editor's content list without
        }

        ed.putItem(ll);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(ll, Editor.MARKERS, element);
    }
}
