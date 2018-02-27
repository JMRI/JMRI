package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.geom.Point2D;
import java.util.List;
import jmri.Turnout;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTurntable;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module handles configuration for display.LayoutTurntable objects for a
 * LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutTurntableXml extends AbstractXmlAdapter {

    public LayoutTurntableXml() {
    }

    /**
     * Default implementation for storing the contents of a LayoutTurntable
     *
     * @param o Object to store, of type LayoutTurntable
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        LayoutTurntable p = (LayoutTurntable) o;

        Element element = new Element("layoutturntable");
        boolean turnoutControl = p.isTurnoutControlled();
        // include attributes
        element.setAttribute("ident", p.getId());
        element.setAttribute("radius", "" + p.getRadius());
        Point2D coords = p.getCoordsCenter();
        element.setAttribute("xcen", "" + coords.getX());
        element.setAttribute("ycen", "" + coords.getY());
        element.setAttribute("turnoutControlled", "" + (turnoutControl ? "yes" : "no"));
        element.setAttribute("class", getClass().getName());
        // add ray tracks
        for (int i = 0; i < p.getNumberRays(); i++) {
            Element rElem = new Element("raytrack");
            rElem.setAttribute("angle", "" + p.getRayAngle(i));
            TrackSegment t = p.getRayConnectOrdered(i);
            if (t != null) {
                rElem.setAttribute("connectname", t.getId());
            }
            rElem.setAttribute("index", "" + p.getRayIndex(i));
            if (turnoutControl && p.getRayTurnoutName(i) != null) {
                rElem.setAttribute("turnout", p.getRayTurnoutName(i));
                if (p.getRayTurnoutState(i) == Turnout.THROWN) {
                    rElem.setAttribute("turnoutstate", "thrown");
                } else {
                    rElem.setAttribute("turnoutstate", "closed");
                }
                if (p.isRayDisabled(i)) {
                    rElem.setAttribute("disabled", "yes");
                }
                if (p.isRayDisabledWhenOccupied(i)) {
                    rElem.setAttribute("disableWhenOccupied", "yes");
                }
            }
            element.addContent(rElem);
        }
        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the layout turntable element, then all the other data
     *
     * @param element Top level Element to unpack.
     * @param o       LayoutEditor as an Object
     */
    @Override
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor) o;

        // get center point
        String name = element.getAttribute("ident").getValue();
        double x = 0.0;
        double y = 0.0;
        double radius = 25.0;
        try {
            x = element.getAttribute("xcen").getFloatValue();
            y = element.getAttribute("ycen").getFloatValue();
            radius = element.getAttribute("radius").getFloatValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert layoutturntable center or radius attributes");
        }
        // create the new LayoutTurntable
        LayoutTurntable l = new LayoutTurntable(name, new Point2D.Double(x, y), p);
        l.setRadius(radius);

        try {
            l.setTurnoutControlled(element.getAttribute("turnoutControlled").getBooleanValue());
        } catch (DataConversionException e1) {
            log.warn("unable to convert layout turnout turnoutControlled attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
        }

        // load ray tracks
        List<Element> rayTrackList = element.getChildren("raytrack");
        if (rayTrackList.size() > 0) {
            for (int i = 0; i < rayTrackList.size(); i++) {
                double angle = 0.0;
                int index = 0;
                Element relem = rayTrackList.get(i);
                try {
                    angle = (relem.getAttribute("angle")).getFloatValue();
                    index = (relem.getAttribute("index")).getIntValue();
                } catch (org.jdom2.DataConversionException e) {
                    log.error("failed to convert ray track angle or index attributes");
                }
                String connectName = "";
                Attribute a = relem.getAttribute("connectname");
                if (a != null) {
                    connectName = a.getValue();
                }
                l.addRayTrack(angle, index, connectName);
                if (l.isTurnoutControlled() && relem.getAttribute("turnout") != null) {
                    if (relem.getAttribute("turnoutstate").getValue().equals("thrown")) {
                        l.setRayTurnout(index, relem.getAttribute("turnout").getValue(), Turnout.THROWN);
                    } else {
                        l.setRayTurnout(index, relem.getAttribute("turnout").getValue(), Turnout.CLOSED);
                    }
                    try {
                        l.setRayDisabled(index, relem.getAttribute("disabled").getBooleanValue());
                    } catch (DataConversionException e1) {
                        log.warn("unable to convert layout turnout disabled attribute");
                    } catch (NullPointerException e) {  // considered normal if the attribute is not present
                    }
                    try {
                        l.setRayDisabledWhenOccupied(index, relem.getAttribute("disableWhenOccupied").getBooleanValue());
                    } catch (DataConversionException e1) {
                        log.warn("unable to convert layout turnout disableWhenOccupied attribute");
                    } catch (NullPointerException e) {  // considered normal if the attribute is not present
                    }
                }
            }
        }
        p.getLayoutTracks().add(l);
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutTurntableXml.class);
}
