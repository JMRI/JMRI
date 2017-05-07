package jmri.jmrit.display.layoutEditor.configurexml;

import static jmri.util.MathUtil.subtract;

import java.awt.geom.Point2D;
import java.util.List;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutFlex;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module handles configuration for display.LayoutFlex objects for a
 * LayoutEditor.
 *
 * @author George Warner Copyright (c) 2017
 */
public class LayoutFlexXml extends AbstractXmlAdapter {

    public LayoutFlexXml() {
    }

    /**
     * Default implementation for storing the contents of a LayoutFlex
     *
     * @param o Object to store, of type LayoutFlex
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        LayoutFlex p = (LayoutFlex) o;

        Element element = new Element("layoutFlex");

        // include attributes
        element.setAttribute("ident", p.getID());
        if (p.getBlockName().length() > 0) {
            element.setAttribute("blockname", p.getBlockName());
        }
        if (p.getConnectA() != null) {
            element.setAttribute("connectaname", ((TrackSegment) p.getConnectA()).getID());
        }
        if (p.getConnectB() != null) {
            element.setAttribute("connectbname", ((TrackSegment) p.getConnectB()).getID());
        }
        Point2D coords = p.getCoordsCenter();
        element.setAttribute("xcen", "" + coords.getX());
        element.setAttribute("ycen", "" + coords.getY());

        coords = p.getCoordsA();
        element.setAttribute("xa", "" + coords.getX());
        element.setAttribute("ya", "" + coords.getY());

        coords = p.getCoordsB();
        element.setAttribute("xb", "" + coords.getX());
        element.setAttribute("yb", "" + coords.getY());

        element.setAttribute("class", getClass().getName());

        // add control points
        Element elementControlpoints = new Element("controlpoints");
        for (int i = 0; i < p.getNumberOfCoords(); i++) {
            Element elementControlpoint = new Element("controlpoint");
            elementControlpoint.setAttribute("index", "" + i);
            coords = subtract(p.getCoordsN(i), p.getCoordsCenter());
            elementControlpoint.setAttribute("x", "" + coords.getX());
            elementControlpoint.setAttribute("y", "" + coords.getY());

            elementControlpoints.addContent(elementControlpoint);
        }
        element.addContent(elementControlpoints);

        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the LayoutFlex element, then all the other data
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
        try {
            x = element.getAttribute("xcen").getFloatValue();
            y = element.getAttribute("ycen").getFloatValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert LayoutFlex center  attribute");
        }

        // create the new LayoutFlex
        LayoutFlex f = new LayoutFlex(name, new Point2D.Double(x, y), p);

        // get remaining attributes
        Attribute a = element.getAttribute("blockname");
        if (a != null) {
            f.tBlockName = a.getValue();
        }

        a = element.getAttribute("connectaname");
        if (a != null) {
            f.connectAName = a.getValue();
        }
        a = element.getAttribute("connectbname");
        if (a != null) {
            f.connectBName = a.getValue();
        }

        try {
            x = element.getAttribute("xa").getFloatValue();
            y = element.getAttribute("ya").getFloatValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert LayoutFlex a coords attribute");
        }
        f.setCoordsA(new Point2D.Double(x, y));

        try {
            x = element.getAttribute("xb").getFloatValue();
            y = element.getAttribute("yb").getFloatValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert LayoutFlex b coords attribute");
        }
        f.setCoordsB(new Point2D.Double(x, y));

        // load control points
        Element controlpointsElement = element.getChild("controlpoints");
        if (null != controlpointsElement) {
            List<Element> elementList = controlpointsElement.getChildren("controlpoint");
            if (null != elementList) {
                if (elementList.size() >= 4) {
                    for (int i = 0; i < elementList.size(); i++) {
                        x = 0.0;
                        y = 0.0;
                        int index = 0;
                        Element relem = elementList.get(i);
                        try {
                            index = (relem.getAttribute("index")).getIntValue();
                            x = (relem.getAttribute("x")).getFloatValue();
                            y = (relem.getAttribute("y")).getFloatValue();
                        } catch (org.jdom2.DataConversionException e) {
                            log.error("failed to convert controlpoint coordinates or index attributes");
                        }
                        f.setControlPointAtIndex(new Point2D.Double(x, y), index);
                    }
                } else {
                    log.error("LayoutFlex four controlpoint elements not found. (found " + elementList.size() + ")");
                }
            } else {
                log.error("LayoutFlex controlpoint elements not found.");
            }
        } else {
            log.error("LayoutFlex controlpoints element not found.");
        }

        p.flexList.add(f);
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutFlexXml.class.getName());
}
