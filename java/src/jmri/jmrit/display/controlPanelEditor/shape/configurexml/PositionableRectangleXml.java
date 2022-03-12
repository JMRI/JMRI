package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import jmri.configurexml.JmriConfigureXmlException;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.controlPanelEditor.shape.PositionableRectangle;

import org.jdom2.Element;

/**
 * Handle configuration for display.PositionableShape objects
 *
 * @author Pete Cressman Copyright (c) 2012
 */
public class PositionableRectangleXml extends PositionableShapeXml {

    public PositionableRectangleXml() {
    }

    /**
     * Default implementation for storing the contents of a PositionableShape
     *
     * @param o Object to store, of type PositionableShape
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        PositionableRectangle p = (PositionableRectangle) o;

        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("positionableRectangle");
        storeCommonAttributes(p, element);

        Element elem = new Element("size");
        elem.setAttribute("width", "" + p.getWidth());
        elem.setAttribute("height", "" + p.getHeight());
        element.addContent(elem);

        element.setAttribute("class", "jmri.jmrit.display.controlPanelEditor.shape.configurexml.PositionableRectangleXml");
        return element;
    }

    /**
     * Create a PositionableShape, then add to a target JLayeredPane
     *
     * @param element Top level Element to unpack.
     * @param o       Editor as an Object
     * @throws JmriConfigureXmlException when a error prevents creating the objects as as
     *                   required by the input XML
     */
    @Override
    public void load(Element element, Object o) throws JmriConfigureXmlException {
        // create the objects
        Editor ed = (Editor) o;
        PositionableRectangle ps = new PositionableRectangle(ed);

        Element elem = element.getChild("size");
        ps.setWidth(getInt(elem, "width"));
        ps.setHeight(getInt(elem, "height"));

        try {
            ed.putItem(ps);
        } catch (Positionable.DuplicateIdException e) {
            throw new JmriConfigureXmlException("Positionable id is not unique", e);
        }
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(ps, Editor.MARKERS, element);
    }
}
