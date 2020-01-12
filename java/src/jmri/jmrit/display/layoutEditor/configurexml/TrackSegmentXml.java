package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTrack;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import jmri.util.ColorUtil;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module handles configuration for display.TrackSegment objects for a
 * LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2019
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
    @Override
    public Element store(Object o) {

        TrackSegment p = (TrackSegment) o;

        Element element = new Element("tracksegment"); // NOI18N

        // include attributes
        element.setAttribute("ident", p.getId());
        if (!p.getBlockName().isEmpty()) {
            element.setAttribute("blockname", p.getBlockName());
        }
        element.setAttribute("connect1name", p.getConnect1Name());
        element.setAttribute("type1", "" + p.getType1());
        element.setAttribute("connect2name", p.getConnect2Name());
        element.setAttribute("type2", "" + p.getType2());
        element.setAttribute("dashed", "" + (p.isDashed() ? "yes" : "no"));
        element.setAttribute("mainline", "" + (p.isMainline() ? "yes" : "no"));
        element.setAttribute("hidden", "" + (p.isHidden() ? "yes" : "no"));
        if (p.isArc()) {
            element.setAttribute("arc", "yes");
            element.setAttribute("flip", "" + (p.isFlip() ? "yes" : "no"));
            element.setAttribute("circle", "" + (p.isCircle() ? "yes" : "no"));
            if ((p.isCircle()) && (p.getAngle() != 0.0D)) {
                element.setAttribute("angle", "" + (p.getAngle()));
                element.setAttribute("hideConLines", "" + (p.hideConstructionLines() ? "yes" : "no"));
            }
        }

        if (p.isBezier()) {
            element.setAttribute("bezier", "yes");
            element.setAttribute("hideConLines", "" + (p.hideConstructionLines() ? "yes" : "no"));
            // add control points
            Element elementControlpoints = new Element("controlpoints");
            for (int i = 0; i < p.getNumberOfBezierControlPoints(); i++) {
                Element elementControlpoint = new Element("controlpoint");

                elementControlpoint.setAttribute("index", "" + i);

                Point2D pt = p.getBezierControlPoint(i);
                elementControlpoint.setAttribute("x", "" + pt.getX());
                elementControlpoint.setAttribute("y", "" + pt.getY());

                elementControlpoints.addContent(elementControlpoint);
            }
            element.addContent(elementControlpoints);
        }

        // store decorations
        Map<String, String> decorations = p.getDecorations();
        if (decorations.size() > 0) {
            Element decorationsElement = new Element("decorations");
            for (Map.Entry<String, String> entry : decorations.entrySet()) {
                String name = entry.getKey();
                if (!name.equals("arrow") && !name.equals("bridge")
                        && !name.equals("bumper") && !name.equals("tunnel")) {
                    Element decorationElement = new Element("decoration");
                    decorationElement.setAttribute("name", name);
                    String value = entry.getValue();
                    if (!value.isEmpty()) {
                        decorationElement.setAttribute("value", value);
                    }
                    decorationsElement.addContent(decorationElement);
                }
            }
            element.addContent(decorationsElement);

            if (p.getArrowStyle() > 0) {
                Element decorationElement = new Element("arrow");
                decorationElement.setAttribute("style", Integer.toString(p.getArrowStyle()));
                if (p.isArrowEndStart() && p.isArrowEndStop()) {
                    decorationElement.setAttribute("end", "both");
                } else if (p.isArrowEndStop()) {
                    decorationElement.setAttribute("end", "stop");
                } else {
                    decorationElement.setAttribute("end", "start");
                }
                if (p.isArrowDirIn() && p.isArrowDirOut()) {
                    decorationElement.setAttribute("direction", "both");
                } else if (p.isArrowDirOut()) {
                    decorationElement.setAttribute("direction", "out");
                } else {
                    decorationElement.setAttribute("direction", "in");
                }
                decorationElement.setAttribute("color", ColorUtil.colorToHexString(p.getArrowColor()));
                decorationElement.setAttribute("linewidth", Integer.toString(p.getArrowLineWidth()));
                decorationElement.setAttribute("length", Integer.toString(p.getArrowLength()));
                decorationElement.setAttribute("gap", Integer.toString(p.getArrowGap()));
                decorationsElement.addContent(decorationElement);
            }
            if (p.isBridgeSideLeft() || p.isBridgeSideRight()) {
                Element decorationElement = new Element("bridge");
                if (p.isBridgeSideLeft() && p.isBridgeSideRight()) {
                    decorationElement.setAttribute("side", "both");
                } else if (p.isBridgeSideLeft()) {
                    decorationElement.setAttribute("side", "left");
                } else {
                    decorationElement.setAttribute("side", "right");
                }
                if (p.isBridgeHasEntry() && p.isBridgeHasExit()) {
                    decorationElement.setAttribute("end", "both");
                } else if (p.isBridgeHasEntry()) {
                    decorationElement.setAttribute("end", "entry");
                } else if (p.isBridgeHasExit()) {
                    decorationElement.setAttribute("end", "exit");
                }
                decorationElement.setAttribute("color", ColorUtil.colorToHexString(p.getBridgeColor()));
                decorationElement.setAttribute("linewidth", Integer.toString(p.getBridgeLineWidth()));
                decorationElement.setAttribute("approachwidth", Integer.toString(p.getBridgeApproachWidth()));
                decorationElement.setAttribute("deckwidth", Integer.toString(p.getBridgeDeckWidth()));
                decorationsElement.addContent(decorationElement);
            }
            if (p.isBumperEndStart() || p.isBumperEndStop()) {
                Element decorationElement = new Element("bumper");
                if (p.isBumperEndStart() && p.isBumperEndStop()) {
                    decorationElement.setAttribute("end", "both");
                } else if (p.isBumperEndStop()) {
                    decorationElement.setAttribute("end", "stop");
                } else {
                    decorationElement.setAttribute("end", "start");
                }
                decorationElement.setAttribute("color", ColorUtil.colorToHexString(p.getBumperColor()));
                decorationElement.setAttribute("linewidth", Integer.toString(p.getBumperLineWidth()));
                decorationElement.setAttribute("length", Integer.toString(p.getBumperLength()));
                if (p.isBumperFlipped()) {
                    decorationElement.setAttribute("flip", "true");
                }
                decorationsElement.addContent(decorationElement);
            }

            if (p.isTunnelSideLeft() || p.isTunnelSideRight()) {
                Element decorationElement = new Element("tunnel");
                if (p.isTunnelSideLeft() && p.isTunnelSideRight()) {
                    decorationElement.setAttribute("side", "both");
                } else if (p.isTunnelSideLeft()) {
                    decorationElement.setAttribute("side", "left");
                } else {
                    decorationElement.setAttribute("side", "right");
                }
                if (p.isTunnelHasEntry() && p.isTunnelHasExit()) {
                    decorationElement.setAttribute("end", "both");
                } else if (p.isTunnelHasEntry()) {
                    decorationElement.setAttribute("end", "entry");
                } else if (p.isTunnelHasExit()) {
                    decorationElement.setAttribute("end", "exit");
                }
                decorationElement.setAttribute("color", ColorUtil.colorToHexString(p.getTunnelColor()));
                decorationElement.setAttribute("linewidth", Integer.toString(p.getTunnelLineWidth()));
                decorationElement.setAttribute("entrancewidth", Integer.toString(p.getTunnelEntranceWidth()));
                decorationElement.setAttribute("floorwidth", Integer.toString(p.getTunnelFloorWidth()));
                decorationsElement.addContent(decorationElement);
            }
        }

        element.setAttribute("class", getClass().getName());

        return element;
    }   // store

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the track segment element, then all all attributes
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
        int type1 = LayoutTrack.NONE;
        int type2 = LayoutTrack.NONE;
        try {
            type1 = element.getAttribute("type1").getIntValue();
            type2 = element.getAttribute("type2").getIntValue();
        } catch (DataConversionException e) {
            log.error("failed to convert tracksegment attribute");
        }

        boolean dash = true;
        try {
            dash = element.getAttribute("dashed").getBooleanValue();
        } catch (DataConversionException e) {
            log.warn("unable to convert track segment dashed attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
        }

        boolean main = true;
        try {
            main = element.getAttribute("mainline").getBooleanValue();
        } catch (DataConversionException e) {
            log.warn("unable to convert track segment mainline attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
        }

        boolean hide = false;
        try {
            hide = element.getAttribute("hidden").getBooleanValue();
        } catch (DataConversionException e) {
            log.warn("unable to convert track segment hidden attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
        }

        String con1Name = element.getAttribute("connect1name").getValue();
        String con2Name = element.getAttribute("connect2name").getValue();

        // create the new TrackSegment
        TrackSegment l = new TrackSegment(name,
                con1Name, type1, con2Name, type2,
                dash, main, hide, p);
        try {
            l.setArc(element.getAttribute("arc").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert track segment arc attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
        }

        if (l.isArc()) {
            try {
                l.setFlip(element.getAttribute("flip").getBooleanValue());
            } catch (DataConversionException e) {
                log.warn("unable to convert track segment flip attribute");
            } catch (NullPointerException e) {  // considered normal if the attribute is not present
            }
            try {
                l.setCircle(element.getAttribute("circle").getBooleanValue());
            } catch (DataConversionException e) {
                log.warn("unable to convert track segment circle attribute");
            } catch (NullPointerException e) {  // considered normal if the attribute is not present
            }
            if (l.isCircle()) {
                try {
                    l.setAngle(element.getAttribute("angle").getDoubleValue());
                } catch (DataConversionException e) {
                    log.error("failed to convert tracksegment attribute");
                } catch (NullPointerException e) {  // considered normal if the attribute is not present
                }
            }
            try {
                if (element.getAttribute("hideConLines").getBooleanValue()) {
                    l.hideConstructionLines(TrackSegment.HIDECON);
                }
            } catch (DataConversionException e) {
                log.warn("unable to convert track segment hideConLines attribute");
            } catch (NullPointerException e) {  // considered normal if the attribute is not present
            }
        }

        try {
            if (element.getAttribute("bezier").getBooleanValue()) {
                // load control points
                Element controlpointsElement = element.getChild("controlpoints");
                if (controlpointsElement != null) {
                    List<Element> elementList = controlpointsElement.getChildren("controlpoint");
                    if (elementList != null) {
                        if (elementList.size() >= 2) {
                            for (int i = 0; i < elementList.size(); i++) {
                                double x = 0.0;
                                double y = 0.0;
                                int index = 0;
                                Element relem = elementList.get(i);
                                try {
                                    index = (relem.getAttribute("index")).getIntValue();
                                    x = (relem.getAttribute("x")).getFloatValue();
                                    y = (relem.getAttribute("y")).getFloatValue();
                                } catch (DataConversionException e) {
                                    log.error("failed to convert controlpoint coordinates or index attributes");
                                }
                                l.setBezierControlPoint(new Point2D.Double(x, y), index);
                            }
                        } else {
                            log.error("Track segment Bezier two controlpoint elements not found. (found " + elementList.size() + ")");
                        }
                    } else {
                        log.error("Track segment Bezier controlpoint elements not found.");
                    }
                } else {
                    log.error("Track segment Bezier controlpoints element not found.");
                }
                // NOTE: do this LAST (so reCenter won't be called yet)
                l.setBezier(true);
            }
        } catch (DataConversionException e) {
            log.error("failed to convert tracksegment attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
        }

        //if (l.getName().equals("T31")) {
        //    log.debug("Stop");
        //}
        // load decorations
        Element decorationsElement = element.getChild("decorations");
        if (decorationsElement != null) {
            List<Element> decorationElementList = decorationsElement.getChildren();
            if (decorationElementList != null) {
                Map<String, String> decorations = new HashMap<>();
                for (Element decorationElement : decorationElementList) {
                    String decorationName = decorationElement.getName();
                    if (decorationName.equals("arrow")) {
                        Attribute a = decorationElement.getAttribute("style");
                        if (a != null) {
                            try {
                                l.setArrowStyle(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }
                        // assume both ends
                        l.setArrowEndStart(true);
                        l.setArrowEndStop(true);
                        a = decorationElement.getAttribute("end");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("start")) {
                                l.setArrowEndStop(false);
                            } else if (value.equals("stop")) {
                                l.setArrowEndStart(false);
                            }
                        }
                        // assume both directions
                        l.setArrowDirIn(true);
                        l.setArrowDirOut(true);
                        a = decorationElement.getAttribute("direction");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("in")) {
                                l.setArrowDirOut(false);
                            } else if (value.equals("out")) {
                                l.setArrowDirIn(false);
                            }
                        }
                        a = decorationElement.getAttribute("color");
                        if (a != null) {
                            try {
                                l.setArrowColor(ColorUtil.stringToColor(a.getValue()));
                            } catch (IllegalArgumentException e) {
                                l.setArrowColor(Color.BLACK);
                                log.error("Invalid color {}; using black", a.getValue());
                            }
                        }
                        a = decorationElement.getAttribute("linewidth");
                        if (a != null) {
                            try {
                                l.setArrowLineWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }
                        a = decorationElement.getAttribute("length");
                        if (a != null) {
                            try {
                                l.setArrowLength(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }
                        a = decorationElement.getAttribute("gap");
                        if (a != null) {
                            try {
                                l.setArrowGap(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }
                    } else if (decorationName.equals("bridge")) {
                        // assume both sides
                        l.setBridgeSideLeft(true);
                        l.setBridgeSideRight(true);
                        Attribute a = decorationElement.getAttribute("side");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("right")) {
                                l.setBridgeSideLeft(false);
                            } else if (value.equals("left")) {
                                l.setBridgeSideRight(false);
                            }
                        }
                        // assume neither end
                        l.setBridgeHasEntry(false);
                        l.setBridgeHasExit(false);
                        a = decorationElement.getAttribute("end");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("both")) {
                                l.setBridgeHasEntry(true);
                                l.setBridgeHasExit(true);
                            } else if (value.equals("entry")) {
                                l.setBridgeHasEntry(true);
                                l.setBridgeHasExit(false);
                            } else if (value.equals("exit")) {
                                l.setBridgeHasEntry(false);
                                l.setBridgeHasExit(true);
                            }
                        }

                        a = decorationElement.getAttribute("color");
                        if (a != null) {
                            try {
                                l.setBridgeColor(ColorUtil.stringToColor(a.getValue()));
                            } catch (IllegalArgumentException e) {
                                l.setBridgeColor(Color.BLACK);
                                log.error("Invalid color {}; using black", a.getValue());
                            }
                        }

                        a = decorationElement.getAttribute("linewidth");
                        if (a != null) {
                            try {
                                l.setBridgeLineWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }

                        a = decorationElement.getAttribute("approachwidth");
                        if (a != null) {
                            try {
                                l.setBridgeApproachWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }

                        a = decorationElement.getAttribute("deckwidth");
                        if (a != null) {
                            try {
                                l.setBridgeDeckWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }
                    } else if (decorationName.equals("bumper")) {
                        // assume both ends
                        l.setBumperEndStart(true);
                        l.setBumperEndStop(true);
                        Attribute a = decorationElement.getAttribute("end");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("start")) {
                                l.setBumperEndStop(false);
                            } else if (value.equals("stop")) {
                                l.setBumperEndStart(false);
                            }
                        }

                        a = decorationElement.getAttribute("color");
                        if (a != null) {
                            try {
                                l.setBumperColor(ColorUtil.stringToColor(a.getValue()));
                            } catch (IllegalArgumentException e) {
                                l.setBumperColor(Color.BLACK);
                                log.error("Invalid color {}; using black", a.getValue());
                            }
                        }

                        a = decorationElement.getAttribute("linewidth");
                        if (a != null) {
                            try {
                                l.setBumperLineWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }

                        a = decorationElement.getAttribute("length");
                        if (a != null) {
                            try {
                                l.setBumperLength(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }

                        a = decorationElement.getAttribute("flip");
                        if (a != null) {
                            try {
                                l.setBumperFlipped(a.getBooleanValue());
                            } catch (DataConversionException e) {
                            }
                        }
                    } else if (decorationName.equals("tunnel")) {
                        // assume both sides
                        l.setTunnelSideLeft(true);
                        l.setTunnelSideRight(true);
                        Attribute a = decorationElement.getAttribute("side");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("right")) {
                                l.setTunnelSideLeft(false);
                            } else if (value.equals("left")) {
                                l.setTunnelSideRight(false);
                            }
                        }
                        // assume neither end
                        l.setTunnelHasEntry(false);
                        l.setTunnelHasExit(false);
                        a = decorationElement.getAttribute("end");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("both")) {
                                l.setTunnelHasEntry(true);
                                l.setTunnelHasExit(true);
                            } else if (value.equals("entry")) {
                                l.setTunnelHasEntry(true);
                                l.setTunnelHasExit(false);
                            } else if (value.equals("exit")) {
                                l.setTunnelHasEntry(false);
                                l.setTunnelHasExit(true);
                            }
                        }

                        a = decorationElement.getAttribute("color");
                        if (a != null) {
                            try {
                                l.setTunnelColor(ColorUtil.stringToColor(a.getValue()));
                            } catch (IllegalArgumentException e) {
                                l.setTunnelColor(Color.BLACK);
                                log.error("Invalid color {}; using black", a.getValue());
                            }
                        }

                        a = decorationElement.getAttribute("linewidth");
                        if (a != null) {
                            try {
                                l.setTunnelLineWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }

                        a = decorationElement.getAttribute("entrancewidth");
                        if (a != null) {
                            try {
                                l.setTunnelEntranceWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }

                        a = decorationElement.getAttribute("floorwidth");
                        if (a != null) {
                            try {
                                l.setTunnelFloorWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }
                    } else {
                        try {
                            String eName = decorationElement.getAttribute("name").getValue();
                            Attribute a = decorationElement.getAttribute("value");
                            String eValue = (a != null) ? a.getValue() : "";
                            decorations.put(eName, eValue);
                        } catch (NullPointerException e) {  // considered normal if the attribute is not present
                            continue;
                        }
                    }
                }
                l.setDecorations(decorations);
            }
        }

        // get remaining attribute
        Attribute a = element.getAttribute("blockname");
        if (a != null) {
            l.tLayoutBlockName = a.getValue();
        }

        p.getLayoutTracks().add(l);
    }

    private final static Logger log
            = LoggerFactory.getLogger(TrackSegmentXml.class);
}
