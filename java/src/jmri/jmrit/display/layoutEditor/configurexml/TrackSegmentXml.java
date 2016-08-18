package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module handles configuration for display.TrackSegment objects for a
 * LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 */
public class TrackSegmentXml extends AbstractXmlAdapter {

    public TrackSegmentXml() {
    }

    /**
     * Default implementation for storing the contents of a TrackSegment
     *
     * @param o Object to store, of type TrackSegment
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        TrackSegment p = (TrackSegment) o;

        Element element = new Element("tracksegment");

        // include attributes
        element.setAttribute("ident", p.getID());
        if (p.getBlockName().length() > 0) {
            element.setAttribute("blockname", p.getBlockName());
        }
        element.setAttribute("connect1name", p.getConnect1Name());
        element.setAttribute("type1", "" + p.getType1());
        element.setAttribute("connect2name", p.getConnect2Name());
        element.setAttribute("type2", "" + p.getType2());
        element.setAttribute("dashed", "" + (p.getDashed() ? "yes" : "no"));
        element.setAttribute("mainline", "" + (p.getMainline() ? "yes" : "no"));
        element.setAttribute("hidden", "" + (p.getHidden() ? "yes" : "no"));
        element.setAttribute("arc", "" + (p.getArc() ? "yes" : "no"));
        if (p.getArc()) {
            element.setAttribute("flip", "" + (p.getFlip() ? "yes" : "no"));
            element.setAttribute("circle", "" + (p.getCircle() ? "yes" : "no"));
            if ((p.getCircle()) && (p.getAngle() != 0.0D)) {
                element.setAttribute("angle", "" + (p.getAngle()));
                element.setAttribute("hideConLines", "" + (p.hideConstructionLines() ? "yes" : "no"));
            }
        }
        element.setAttribute("class", getClass().getName());
        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the tracksegment element, then all all attributes
     *
     * @param element Top level Element to unpack.
     * @param o       LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor) o;

        // get attributes
        String name = element.getAttribute("ident").getValue();
        int type1 = LayoutEditor.NONE;
        int type2 = LayoutEditor.NONE;
        try {
            type1 = element.getAttribute("type1").getIntValue();
            type2 = element.getAttribute("type2").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert tracksegment attribute");
        }
        boolean dash = true;
        if (element.getAttribute("dashed").getValue().equals("no")) {
            dash = false;
        }
        boolean main = true;
        if (element.getAttribute("mainline").getValue().equals("no")) {
            main = false;
        }
        boolean hide = true;
        if (element.getAttribute("hidden").getValue().equals("no")) {
            hide = false;
        }
        String con1Name = element.getAttribute("connect1name").getValue();
        String con2Name = element.getAttribute("connect2name").getValue();

        // create the new TrackSegment
        TrackSegment l = new TrackSegment(name, con1Name, type1, con2Name, type2,
                dash, main, hide, p);
        try {
            if (element.getAttribute("arc").getValue().equals("yes")) {
                l.setArc(true);
            }
        } catch (NullPointerException e) {
        }//considered normal if the attribute is not present }
        if (l.getArc()) {
            //int angle = 0;
            //int startangle = 0;
            try {
                if (element.getAttribute("flip").getValue().equals("yes")) {
                    l.setFlip(true);
                }
            } catch (NullPointerException e) {
            }//considered normal if the attribute is not present }
            try {
                if (element.getAttribute("circle").getValue().equals("yes")) {
                    l.setCircle(true);
                }
            } catch (NullPointerException e) {
            }
            if (l.getCircle()) {
                try {
                    l.setAngle(element.getAttribute("angle").getDoubleValue());
                } catch (org.jdom2.DataConversionException e) {
                    log.error("failed to convert tracksegment attribute");
                } catch (NullPointerException e) {  // considered normal if the attribute not present
                }
            }
            try {
                if (element.getAttribute("hideConLines").getValue().equals("yes")) {
                    l.hideConstructionLines(TrackSegment.HIDECON);
                }
            } catch (NullPointerException e) {
            }//considered normal if the attribute is not present }
        }
        // get remaining attribute
        Attribute a = element.getAttribute("blockname");
        if (a != null) {
            l.tBlockName = a.getValue();
        }
        p.trackList.add(l);
    }

    private final static Logger log = LoggerFactory.getLogger(TrackSegmentXml.class.getName());
}
