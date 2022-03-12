package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.controlPanelEditor.shape.PositionableRoundRect;

import org.jdom2.Element;

/**
 * Handle configuration for display.PositionableShape objects
 *
 * @author Pete Cressman Copyright (c) 2012
 */
public class PositionableRoundRectXml extends PositionableShapeXml {

    public PositionableRoundRectXml() {
    }

    /**
     * Default implementation for storing the contents of a PositionableShape
     *
     * @param o Object to store, of type PositionableShape
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        PositionableRoundRect p = (PositionableRoundRect) o;

        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("positionableRoundRect");
        storeCommonAttributes(p, element);

        Element elem = new Element("size");
        elem.setAttribute("width", "" + p.getWidth());
        elem.setAttribute("height", "" + p.getHeight());
        elem.setAttribute("cornerRadius", "" + p.getCornerRadius());
        element.addContent(elem);

        element.setAttribute("class", "jmri.jmrit.display.controlPanelEditor.shape.configurexml.PositionableRoundRectXml");
        return element;
    }

    /**
     * Create a PositionableShape, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     */
    @Override
    public void load(Element element, Object o) throws JmriConfigureXmlException {
        // create the objects
        Editor ed = (Editor) o;
        PositionableRoundRect ps = new PositionableRoundRect(ed);

        Element elem = element.getChild("size");
        ps.setWidth(getInt(elem, "width"));
        ps.setHeight(getInt(elem, "height"));
        ps.setCornerRadius(getInt(elem, "cornerRadius"));

        try {
            ed.putItem(ps);
        } catch (Positionable.DuplicateIdException e) {
            throw new JmriConfigureXmlException("Positionable id is not unique", e);
        }
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(ps, Editor.MARKERS, element);
    }
}
