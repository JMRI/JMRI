package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.*;
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
 * @author Bob Jacobsen Copyright (c) 2020
 * @author David Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2019
 */
public class TrackSegmentViewXml extends AbstractXmlAdapter {

    public TrackSegmentViewXml() {
    }

    /**
     * Default implementation for storing the contents of a TrackSegment
     *
     * @param o Object to store, of type TrackSegment
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        TrackSegmentView view = (TrackSegmentView) o;
        TrackSegment trk = view.getTrackSegment();

        Element element = new Element("tracksegment"); // NOI18N

        // include attributes
        element.setAttribute("ident", trk.getId());
        if (!trk.getBlockName().isEmpty()) {
            element.setAttribute("blockname", trk.getBlockName());
        }

        element.setAttribute("connect1name", trk.getConnect1Name());
        element.setAttribute("type1", "" + htpMap.outputFromEnum(trk.getType1()) );
        element.setAttribute("connect2name", trk.getConnect2Name());
        element.setAttribute("type2", "" + htpMap.outputFromEnum(trk.getType2()) );

        element.setAttribute("dashed",      (view.isDashed() ? "yes" : "no"));
        element.setAttribute("mainline",    (trk.isMainline() ? "yes" : "no"));
        element.setAttribute("hidden",      (view.isHidden() ? "yes" : "no"));

        if (view.isArc()) {
            element.setAttribute("arc",         "yes");
            element.setAttribute("flip",        (view.isFlip() ? "yes" : "no"));
            element.setAttribute("circle",      (view.isCircle() ? "yes" : "no"));
            if (view.isCircle()) {
                element.setAttribute("angle",   "" + (view.getAngle()));
                element.setAttribute("hideConLines", (view.hideConstructionLines() ? "yes" : "no"));
            }
        }

        if (view.isBezier()) {
            element.setAttribute("bezier", "yes");
            element.setAttribute("hideConLines", "" + (view.hideConstructionLines() ? "yes" : "no"));
            // add control points
            Element elementControlpoints = new Element("controlpoints");
            for (int i = 0; i < view.getNumberOfBezierControlPoints(); i++) {
                Element elementControlpoint = new Element("controlpoint");

                elementControlpoint.setAttribute("index", "" + i);

                Point2D pt = view.getBezierControlPoint(i);
                // correctly working code copied from PositionablePointVewXml:
                DecimalFormat twoDecFormat = new DecimalFormat("#.##");
                elementControlpoint.setAttribute("x", ""+Float.valueOf(twoDecFormat.format(pt.getX())));
                elementControlpoint.setAttribute("y", ""+Float.valueOf(twoDecFormat.format(pt.getY())));
                // String.format follows the Locale of JMRI, resulting in ',' as decimal separator = invalid xml
                elementControlpoints.addContent(elementControlpoint);
            }
            element.addContent(elementControlpoints);
        }

        // store decorations
        Map<String, String> decorations = view.getDecorations();
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

            if (view.getArrowStyle() > 0) {
                Element decorationElement = new Element("arrow");
                decorationElement.setAttribute("style", Integer.toString(view.getArrowStyle()));
                if (view.isArrowEndStart() && view.isArrowEndStop()) {
                    decorationElement.setAttribute("end", "both");
                } else if (view.isArrowEndStop()) {
                    decorationElement.setAttribute("end", "stop");
                } else {
                    decorationElement.setAttribute("end", "start");
                }
                if (view.isArrowDirIn() && view.isArrowDirOut()) {
                    decorationElement.setAttribute("direction", "both");
                } else if (view.isArrowDirOut()) {
                    decorationElement.setAttribute("direction", "out");
                } else {
                    decorationElement.setAttribute("direction", "in");
                }
                decorationElement.setAttribute("color", ColorUtil.colorToHexString(view.getArrowColor()));
                decorationElement.setAttribute("linewidth", Integer.toString(view.getArrowLineWidth()));
                decorationElement.setAttribute("length", Integer.toString(view.getArrowLength()));
                decorationElement.setAttribute("gap", Integer.toString(view.getArrowGap()));
                decorationsElement.addContent(decorationElement);
            }
            if (view.isBridgeSideLeft() || view.isBridgeSideRight()) {
                Element decorationElement = new Element("bridge");
                if (view.isBridgeSideLeft() && view.isBridgeSideRight()) {
                    decorationElement.setAttribute("side", "both");
                } else if (view.isBridgeSideLeft()) {
                    decorationElement.setAttribute("side", "left");
                } else {
                    decorationElement.setAttribute("side", "right");
                }
                if (view.isBridgeHasEntry() && view.isBridgeHasExit()) {
                    decorationElement.setAttribute("end", "both");
                } else if (view.isBridgeHasEntry()) {
                    decorationElement.setAttribute("end", "entry");
                } else if (view.isBridgeHasExit()) {
                    decorationElement.setAttribute("end", "exit");
                }
                decorationElement.setAttribute("color", ColorUtil.colorToHexString(view.getBridgeColor()));
                decorationElement.setAttribute("linewidth", Integer.toString(view.getBridgeLineWidth()));
                decorationElement.setAttribute("approachwidth", Integer.toString(view.getBridgeApproachWidth()));
                decorationElement.setAttribute("deckwidth", Integer.toString(view.getBridgeDeckWidth()));
                decorationsElement.addContent(decorationElement);
            }
            if (view.isBumperEndStart() || view.isBumperEndStop()) {
                Element decorationElement = new Element("bumper");
                if (view.isBumperEndStart() && view.isBumperEndStop()) {
                    decorationElement.setAttribute("end", "both");
                } else if (view.isBumperEndStop()) {
                    decorationElement.setAttribute("end", "stop");
                } else {
                    decorationElement.setAttribute("end", "start");
                }
                decorationElement.setAttribute("color", ColorUtil.colorToHexString(view.getBumperColor()));
                decorationElement.setAttribute("linewidth", Integer.toString(view.getBumperLineWidth()));
                decorationElement.setAttribute("length", Integer.toString(view.getBumperLength()));
                if (view.isBumperFlipped()) {
                    decorationElement.setAttribute("flip", "true");
                }
                decorationsElement.addContent(decorationElement);
            }

            if (view.isTunnelSideLeft() || view.isTunnelSideRight()) {
                Element decorationElement = new Element("tunnel");
                if (view.isTunnelSideLeft() && view.isTunnelSideRight()) {
                    decorationElement.setAttribute("side", "both");
                } else if (view.isTunnelSideLeft()) {
                    decorationElement.setAttribute("side", "left");
                } else {
                    decorationElement.setAttribute("side", "right");
                }
                if (view.isTunnelHasEntry() && view.isTunnelHasExit()) {
                    decorationElement.setAttribute("end", "both");
                } else if (view.isTunnelHasEntry()) {
                    decorationElement.setAttribute("end", "entry");
                } else if (view.isTunnelHasExit()) {
                    decorationElement.setAttribute("end", "exit");
                }
                decorationElement.setAttribute("color", ColorUtil.colorToHexString(view.getTunnelColor()));
                decorationElement.setAttribute("linewidth", Integer.toString(view.getTunnelLineWidth()));
                decorationElement.setAttribute("entrancewidth", Integer.toString(view.getTunnelEntranceWidth()));
                decorationElement.setAttribute("floorwidth", Integer.toString(view.getTunnelFloorWidth()));
                decorationsElement.addContent(decorationElement);
            }
        }

        //element.setAttribute("class", getClass().getName());
        log.debug("storing old fixed class name for TrackSegment");
        element.setAttribute("class", "jmri.jmrit.display.layoutEditor.configurexml.TrackSegmentXml");

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

        boolean dash = getAttributeBooleanValue(element, "dashed", true);
        boolean main = getAttributeBooleanValue(element, "mainline", true);
        boolean hide = getAttributeBooleanValue(element, "hidden", false);

        String con1Name = element.getAttribute("connect1name").getValue();
        String con2Name = element.getAttribute("connect2name").getValue();


        HitPointType type1 = htpMap.inputFromAttribute(element.getAttribute("type1"));

        HitPointType type2 = htpMap.inputFromAttribute(element.getAttribute("type2"));

        // create the new TrackSegment and view
        TrackSegment lt = new TrackSegment(name,
                con1Name, type1, con2Name, type2,
                main, p);
        TrackSegmentView lv = new TrackSegmentView(lt, p);
        lv.setHidden(hide);

        lv.setDashed(dash);
        lv.setArc( getAttributeBooleanValue(element, "arc", false) );

        if (lv.isArc()) {
            lv.setFlip( getAttributeBooleanValue(element, "flip", false) );
            lv.setCircle( getAttributeBooleanValue(element, "circle", false) );
            if (lv.isCircle()) {
                lv.setAngle( getAttributeDoubleValue(element, "angle", 0.0) );
            }
        }

        if ( getAttributeBooleanValue(element, "bezier", false)) {
            // load control points
            Element controlpointsElement = element.getChild("controlpoints");
            if (controlpointsElement != null) {
                List<Element> elementList = controlpointsElement.getChildren("controlpoint");
                if (elementList != null) {
                    if (elementList.size() >= 2) {
                        for (Element value : elementList) {
                            double x = 0.0;
                            double y = 0.0;
                            int index = 0;
                            try {
                                index = (value.getAttribute("index")).getIntValue();
                                x = (value.getAttribute("x")).getFloatValue();
                                y = (value.getAttribute("y")).getFloatValue();
                            } catch (DataConversionException e) {
                                log.error("failed to convert controlpoint coordinates or index attributes");
                            }
                            lv.setBezierControlPoint(new Point2D.Double(x, y), index);
                        }
                    } else {
                        log.error("Track segment Bezier two controlpoint elements not found. (found {})", elementList.size());
                    }
                } else {
                    log.error("Track segment Bezier controlpoint elements not found.");
                }
            } else {
                log.error("Track segment Bezier controlpoints element not found.");
            }
            // NOTE: do this LAST (so reCenter won't be called yet)
            lv.setBezier(true);
        }

        if ( getAttributeBooleanValue(element, "hideConLines", false) ) {
            lv.hideConstructionLines(TrackSegmentView.HIDECON);
        }
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
                                lv.setArrowStyle(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }
                        // assume both ends
                        lv.setArrowEndStart(true);
                        lv.setArrowEndStop(true);
                        a = decorationElement.getAttribute("end");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("start")) {
                                lv.setArrowEndStop(false);
                            } else if (value.equals("stop")) {
                                lv.setArrowEndStart(false);
                            }
                        }
                        // assume both directions
                        lv.setArrowDirIn(true);
                        lv.setArrowDirOut(true);
                        a = decorationElement.getAttribute("direction");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("in")) {
                                lv.setArrowDirOut(false);
                            } else if (value.equals("out")) {
                                lv.setArrowDirIn(false);
                            }
                        }
                        a = decorationElement.getAttribute("color");
                        if (a != null) {
                            try {
                                lv.setArrowColor(ColorUtil.stringToColor(a.getValue()));
                            } catch (IllegalArgumentException e) {
                                lv.setArrowColor(Color.BLACK);
                                log.error("Invalid color {}; using black", a.getValue());
                            }
                        }
                        a = decorationElement.getAttribute("linewidth");
                        if (a != null) {
                            try {
                                lv.setArrowLineWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }
                        a = decorationElement.getAttribute("length");
                        if (a != null) {
                            try {
                                lv.setArrowLength(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }
                        a = decorationElement.getAttribute("gap");
                        if (a != null) {
                            try {
                                lv.setArrowGap(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }
                    } else if (decorationName.equals("bridge")) {
                        // assume both sides
                        lv.setBridgeSideLeft(true);
                        lv.setBridgeSideRight(true);
                        Attribute a = decorationElement.getAttribute("side");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("right")) {
                                lv.setBridgeSideLeft(false);
                            } else if (value.equals("left")) {
                                lv.setBridgeSideRight(false);
                            }
                        }
                        // assume neither end
                        lv.setBridgeHasEntry(false);
                        lv.setBridgeHasExit(false);
                        a = decorationElement.getAttribute("end");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("both")) {
                                lv.setBridgeHasEntry(true);
                                lv.setBridgeHasExit(true);
                            } else if (value.equals("entry")) {
                                lv.setBridgeHasEntry(true);
                                lv.setBridgeHasExit(false);
                            } else if (value.equals("exit")) {
                                lv.setBridgeHasEntry(false);
                                lv.setBridgeHasExit(true);
                            }
                        }

                        a = decorationElement.getAttribute("color");
                        if (a != null) {
                            try {
                                lv.setBridgeColor(ColorUtil.stringToColor(a.getValue()));
                            } catch (IllegalArgumentException e) {
                                lv.setBridgeColor(Color.BLACK);
                                log.error("Invalid color {}; using black", a.getValue());
                            }
                        }

                        a = decorationElement.getAttribute("linewidth");
                        if (a != null) {
                            try {
                                lv.setBridgeLineWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }

                        a = decorationElement.getAttribute("approachwidth");
                        if (a != null) {
                            try {
                                lv.setBridgeApproachWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }

                        a = decorationElement.getAttribute("deckwidth");
                        if (a != null) {
                            try {
                                lv.setBridgeDeckWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }
                    } else if (decorationName.equals("bumper")) {
                        // assume both ends
                        lv.setBumperEndStart(true);
                        lv.setBumperEndStop(true);
                        Attribute a = decorationElement.getAttribute("end");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("start")) {
                                lv.setBumperEndStop(false);
                            } else if (value.equals("stop")) {
                                lv.setBumperEndStart(false);
                            }
                        }

                        a = decorationElement.getAttribute("color");
                        if (a != null) {
                            try {
                                lv.setBumperColor(ColorUtil.stringToColor(a.getValue()));
                            } catch (IllegalArgumentException e) {
                                lv.setBumperColor(Color.BLACK);
                                log.error("Invalid color {}; using black", a.getValue());
                            }
                        }

                        a = decorationElement.getAttribute("linewidth");
                        if (a != null) {
                            try {
                                lv.setBumperLineWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }

                        a = decorationElement.getAttribute("length");
                        if (a != null) {
                            try {
                                lv.setBumperLength(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }

                        a = decorationElement.getAttribute("flip");
                        if (a != null) {
                            try {
                                lv.setBumperFlipped(a.getBooleanValue());
                            } catch (DataConversionException e) {
                            }
                        }
                    } else if (decorationName.equals("tunnel")) {
                        // assume both sides
                        lv.setTunnelSideLeft(true);
                        lv.setTunnelSideRight(true);
                        Attribute a = decorationElement.getAttribute("side");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("right")) {
                                lv.setTunnelSideLeft(false);
                            } else if (value.equals("left")) {
                                lv.setTunnelSideRight(false);
                            }
                        }
                        // assume neither end
                        lv.setTunnelHasEntry(false);
                        lv.setTunnelHasExit(false);
                        a = decorationElement.getAttribute("end");
                        if (a != null) {
                            String value = a.getValue();
                            if (value.equals("both")) {
                                lv.setTunnelHasEntry(true);
                                lv.setTunnelHasExit(true);
                            } else if (value.equals("entry")) {
                                lv.setTunnelHasEntry(true);
                                lv.setTunnelHasExit(false);
                            } else if (value.equals("exit")) {
                                lv.setTunnelHasEntry(false);
                                lv.setTunnelHasExit(true);
                            }
                        }

                        a = decorationElement.getAttribute("color");
                        if (a != null) {
                            try {
                                lv.setTunnelColor(ColorUtil.stringToColor(a.getValue()));
                            } catch (IllegalArgumentException e) {
                                lv.setTunnelColor(Color.BLACK);
                                log.error("Invalid color {}; using black", a.getValue());
                            }
                        }

                        a = decorationElement.getAttribute("linewidth");
                        if (a != null) {
                            try {
                                lv.setTunnelLineWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }

                        a = decorationElement.getAttribute("entrancewidth");
                        if (a != null) {
                            try {
                                lv.setTunnelEntranceWidth(a.getIntValue());
                            } catch (DataConversionException e) {
                            }
                        }

                        a = decorationElement.getAttribute("floorwidth");
                        if (a != null) {
                            try {
                                lv.setTunnelFloorWidth(a.getIntValue());
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
                        }
                    }
                }
                lv.setDecorations(decorations);
            }
        }

        // get remaining attribute
        Attribute a = element.getAttribute("blockname");
        if (a != null) {
            lt.tLayoutBlockName = a.getValue();
        }

        p.addLayoutTrack(lt, lv);
    }

    static final EnumIO<HitPointType> htpMap = new EnumIoNamesNumbers<>(HitPointType.class);

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TrackSegmentViewXml.class);
}
