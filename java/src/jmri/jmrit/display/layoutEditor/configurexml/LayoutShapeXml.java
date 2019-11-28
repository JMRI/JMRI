package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutShape;
import jmri.util.ColorUtil;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module handles configuration for LayoutShape objects for a LayoutEditor.
 *
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutShapeXml extends AbstractXmlAdapter {

    public LayoutShapeXml() {
    }

    /**
     * Default implementation for storing the contents of a LayoutShape
     *
     * @param o Object to store, of type LayoutShape
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        LayoutShape s = (LayoutShape) o;
        Element element = null;

        if (s.getNumberPoints() > 0) {
            element = new Element("layoutShape");

            // include attributes
            element.setAttribute("ident", s.getName());
            element.setAttribute("type", "" + s.getType().getName());
            element.setAttribute("level", "" + s.getLevel());
            element.setAttribute("linewidth", "" + s.getLineWidth());
            element.setAttribute("lineColor", ColorUtil.colorToHexString(s.getLineColor()));
            element.setAttribute("fillColor", ColorUtil.colorToHexString(s.getFillColor()));

            Element elementPoints = new Element("points");
            ArrayList<LayoutShape.LayoutShapePoint> shapePoints = s.getPoints();
            for (LayoutShape.LayoutShapePoint p : shapePoints) {
                Element elementPoint = new Element("point");

                elementPoint.setAttribute("type", "" + p.getType().getName());

                Point2D pt = p.getPoint();
                elementPoint.setAttribute("x", "" + pt.getX());
                elementPoint.setAttribute("y", "" + pt.getY());

                elementPoints.addContent(elementPoint);
            }
            element.addContent(elementPoints);

            element.setAttribute("class", getClass().getName());
        }
        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the LayoutShape element, then all the other data
     *
     * @param element Top level Element to unpack.
     * @param o       LayoutEditor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor) o;

        String name = element.getAttribute("ident").getValue();

        LayoutShape.LayoutShapeType type = LayoutShape.LayoutShapeType.eOpen;
        try {
            type = LayoutShape.LayoutShapeType.getName(element.getAttribute("type").getValue());
        } catch (java.lang.NullPointerException e) {
            log.error("Layout Shape type attribute not found.");
        }

        // create the new LayoutShape
        LayoutShape s = new LayoutShape(name, type, p);

        Attribute a = element.getAttribute("level");
        if (a != null) {
            try {
                s.setLevel(a.getIntValue());
            } catch (DataConversionException e) {
                log.error("Layout Shape level attribute Conversion error.");
            }
        } else {
            log.error("Layout Shape level attribute not found.");
        }

        a = element.getAttribute("linewidth");
        if (a != null) {
            try {
                s.setLineWidth(a.getIntValue());
            } catch (DataConversionException e) {
                log.error("Layout Shape line width attribute Conversion error.");
            }
        } else {
            log.error("Layout Shape line width attribute not found.");
        }

        a = element.getAttribute("lineColor");
        if (a != null) {
            try {
                s.setLineColor(ColorUtil.stringToColor(a.getValue()));
            } catch (IllegalArgumentException e) {
                s.setLineColor(Color.BLACK);
                log.error("Invalid lineColor {}; using black", a.getValue());
            }
        }

        a = element.getAttribute("fillColor");
        if (a != null) {
            try {
                s.setFillColor(ColorUtil.stringToColor(a.getValue()));
            } catch (IllegalArgumentException e) {
                s.setFillColor(Color.BLACK);
                log.error("Invalid fillColor {}; using black", a.getValue());
            }
        }

        Element pointsElement = element.getChild("points");
        if (pointsElement != null) {
            List<Element> elementList = pointsElement.getChildren("point");
            if (elementList != null) {
                if (elementList.size() > 0) {
                    for (int i = 0; i < elementList.size(); i++) {
                        Element relem = elementList.get(i);

                        LayoutShape.LayoutShapePointType pointType = LayoutShape.LayoutShapePointType.eStraight;
                        try {
                            pointType = LayoutShape.LayoutShapePointType.getName(relem.getAttribute("type").getValue());
                        } catch (java.lang.NullPointerException e) {
                            log.error("Layout Shape Point #" + i + "type attribute not found.");
                        }
                        double x = 0.0;
                        double y = 0.0;
                        try {
                            x = (relem.getAttribute("x")).getFloatValue();
                            y = (relem.getAttribute("y")).getFloatValue();
                        } catch (DataConversionException e) {
                            log.error("failed to convert Layout Shape point #" + i + "coordinates attributes");
                        }
                        s.addPoint(pointType, new Point2D.Double(x, y));
                    }
                } else {
                    log.error("No Layout Shape point elements");
                }
            } else {
                log.error("Layout Shape point elements not found.");
            }
        } else {
            log.error("Layout Shape points element not found.");
        }
        p.getLayoutShapes().add(s);
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutShapeXml.class);
}
