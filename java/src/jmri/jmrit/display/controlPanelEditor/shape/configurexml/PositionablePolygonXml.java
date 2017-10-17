package jmri.jmrit.display.controlPanelEditor.shape.configurexml;

import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.shape.PositionablePolygon;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for display.PositionableShape objects
 *
 * @author Pete Cressman Copyright (c) 2012
 */
public class PositionablePolygonXml extends PositionableShapeXml {

    public PositionablePolygonXml() {
    }

    /**
     * Default implementation for storing the contents of a PositionableShape
     *
     * @param o Object to store, of type PositionableShape
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        PositionablePolygon p = (PositionablePolygon) o;

        if (!p.isActive()) {
            return null;  // if flagged as inactive, don't store
        }
        Element element = new Element("positionablePolygon");
        storeCommonAttributes(p, element);
        element.addContent(storePath(p));

        element.setAttribute("class", "jmri.jmrit.display.controlPanelEditor.shape.configurexml.PositionablePolygonXml");
        return element;
    }

    protected Element storePath(PositionablePolygon p) {
        Element elem = new Element("path");
        PathIterator iter = p.getPathIterator(null);
        float[] coord = new float[6];
        while (!iter.isDone()) {
            int type = iter.currentSegment(coord);
            elem.addContent(storeVertex(type, coord));
            iter.next();
        }
        return elem;
    }

    private Element storeVertex(int type, float[] coord) {
        Element elem = new Element("vertex");
        elem.setAttribute("type", String.valueOf(type));
        for (int i = 0; i < coord.length; i++) {
            elem.setAttribute("idx" + i, String.valueOf(coord[i]));
        }
        return elem;
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
        GeneralPath path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
        Element elem = element.getChild("path");

        float[] coord = new float[6];
        java.util.List<Element> list = elem.getChildren("vertex");
        for (int j = 0; j < list.size(); j++) {
            Element e = list.get(j);
            int type = getInt(e, "type");
            for (int i = 0; i < coord.length; i++) {
                coord[i] = getFloat(e, "idx" + i);
            }
            switch (type) {
                case PathIterator.SEG_MOVETO:
                    path.moveTo(coord[0], coord[1]);
                    break;
                case PathIterator.SEG_LINETO:
                    path.lineTo(coord[0], coord[1]);
                    break;
                case PathIterator.SEG_QUADTO:
                    path.quadTo(coord[0], coord[1], coord[2], coord[3]);
                    break;
                case PathIterator.SEG_CUBICTO:
                    path.curveTo(coord[0], coord[1], coord[2], coord[3], coord[4], coord[5]);
                    break;
                case PathIterator.SEG_CLOSE:
                    path.closePath();
                    break;
                default:
                    log.warn("Unhandled type: {}", type);
                    break;
            }
        }
        PositionablePolygon ps = new PositionablePolygon(ed, path);
        // get object class and determine editor being used
        ed.putItem(ps);
        // load individual item's option settings after editor has set its global settings
        loadCommonAttributes(ps, Editor.MARKERS, element);
    }
    private final static Logger log = LoggerFactory.getLogger(PositionablePolygonXml.class);
}
