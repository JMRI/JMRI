package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.geom.Point2D;
import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.*;
import org.jdom2.Attribute;
import org.jdom2.Element;

import javax.annotation.Nonnull;

/**
 * This module handles configuration for display.PositionablePoint objects for a
 * LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2018
 */
public class PositionablePointViewXml extends AbstractXmlAdapter {

    static final EnumIO<PositionablePoint.PointType> pTypeEnumMap = new EnumIoNamesNumbers<>(PositionablePoint.PointType.class);

    public PositionablePointViewXml() {
    }

    /**
     * Default implementation for storing the contents of a PositionablePoint
     *
     * @param o Object to store, of type PositionablePoint
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        PositionablePointView pv = (PositionablePointView) o;
        PositionablePoint p = pv.getPoint();

        Element element = new Element("positionablepoint");

        // include attributes
        element.setAttribute("ident", p.getId());
        element.setAttribute("type", pTypeEnumMap.outputFromEnum(p.getType()));
        Point2D coords = pv.getCoordsCenter();
        element.setAttribute("x", "" + coords.getX());
        element.setAttribute("y", "" + coords.getY());
        if (p.getConnect1() != null) {
            element.setAttribute("connect1name", p.getConnect1().getId());
        }
        if (p.getConnect2() != null) {
            element.setAttribute("connect2name", p.getConnect2().getId());
        }
        if (!p.getEastBoundSignal().isEmpty()) {
            element.setAttribute("eastboundsignal", p.getEastBoundSignal());
        }
        if (!p.getWestBoundSignal().isEmpty()) {
            element.setAttribute("westboundsignal", p.getWestBoundSignal());
        }

        if (!p.getEastBoundSignalMastName().isEmpty()) {
            element.setAttribute("eastboundsignalmast", p.getEastBoundSignalMastName());
        }
        if (!p.getWestBoundSignalMastName().isEmpty()) {
            element.setAttribute("westboundsignalmast", p.getWestBoundSignalMastName());
        }

        if (!p.getEastBoundSensorName().isEmpty()) {
            element.setAttribute("eastboundsensor", p.getEastBoundSensorName());
        }
        if (!p.getWestBoundSensorName().isEmpty()) {
            element.setAttribute("westboundsensor", p.getWestBoundSensorName());
        }
        if (p.getType() == PositionablePoint.PointType.EDGE_CONNECTOR) {
            element.setAttribute("linkedpanel", p.getLinkedEditorName());
            element.setAttribute("linkpointid", p.getLinkedPointId());
        }

        element.setAttribute("class", "jmri.jmrit.display.layoutEditor.configurexml.PositionablePointXml");
        return element;
    }

    @Override
    public boolean load(@Nonnull Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the layoutblock element, then all the value-icon
     * pairs
     *
     * @param element Top level Element to unpack.
     * @param o       LayoutEditor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor) o;

        // get attributes
        String name = element.getAttribute("ident").getValue();
        PositionablePoint.PointType type = PositionablePoint.PointType.ANCHOR;
        double x = 0.0;
        double y = 0.0;
        try {
            x = element.getAttribute("x").getFloatValue();
            y = element.getAttribute("y").getFloatValue();
            type = pTypeEnumMap.inputFromAttribute(element.getAttribute("type"));
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert positionablepoint attribute");
        }

        // create the new PositionablePoint
        PositionablePoint l = new PositionablePoint(name, type, p);
        PositionablePointView pv = new PositionablePointView(l, new Point2D.Double(x, y), p);
        p.addLayoutTrack(l, pv);

        // get remaining attributes
        Attribute a = element.getAttribute("connect1name");
        if (a != null) {
            l.trackSegment1Name = a.getValue();
        }
        a = element.getAttribute("connect2name");
        if (a != null) {
            l.trackSegment2Name = a.getValue();
        }
        a = element.getAttribute("eastboundsignal");
        if (a != null) {
            l.setEastBoundSignal(a.getValue());
        }
        a = element.getAttribute("westboundsignal");
        if (a != null) {
            l.setWestBoundSignal(a.getValue());
        }
        a = element.getAttribute("eastboundsignalmast");
        if (a != null) {
            l.setEastBoundSignalMast(a.getValue());
        }
        a = element.getAttribute("westboundsignalmast");
        if (a != null) {
            l.setWestBoundSignalMast(a.getValue());
        }
        a = element.getAttribute("eastboundsensor");
        if (a != null) {
            l.setEastBoundSensor(a.getValue());
        }
        a = element.getAttribute("westboundsensor");
        if (a != null) {
            l.setWestBoundSensor(a.getValue());
        }

        if (type == PositionablePoint.PointType.EDGE_CONNECTOR && element.getAttribute("linkedpanel") != null && element.getAttribute("linkpointid") != null) {
            String linkedEditorName = element.getAttribute("linkedpanel").getValue();
            LayoutEditor linkedEditor = InstanceManager.getDefault(EditorManager.class).get(LayoutEditor.class, linkedEditorName);
            if (linkedEditor != null) {
                String linkedPoint = element.getAttribute("linkpointid").getValue();
                for (PositionablePoint point : linkedEditor.getPositionablePoints()) {
                    if (point.getType() == PositionablePoint.PointType.EDGE_CONNECTOR && point.getId().equals(linkedPoint)) {
                        point.setLinkedPoint(l);
                        l.setLinkedPoint(point);
                        break;
                    }
                }
            }
        }

    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PositionablePointViewXml.class);
}
