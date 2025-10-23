package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.geom.Point2D;
import java.util.List;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTraverser;
import jmri.jmrit.display.layoutEditor.LayoutTraverserView;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;

/**
 * This module handles configuration for display.LayoutTraverser objects for a
 * LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2018
 * @author Bob Jacobsen  Copyright (c) 2020
 */
public class LayoutTraverserViewXml extends LayoutTrackViewXml {

    public LayoutTraverserViewXml() {
    }

    /**
     * Default implementation for storing the contents of a LayoutTraverser
     *
     * @param o Object to store, of type LayoutTraverser
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        LayoutTraverserView pv = (LayoutTraverserView) o;
        LayoutTraverser lt = pv.getTraverser();

        Element element = new Element("layouttraverser");
        boolean turnoutControl = lt.isTurnoutControlled();
        // include attributes
        element.setAttribute("ident", lt.getName());
        if (!lt.getBlockName().isEmpty()) {
            element.setAttribute("blockname", lt.getBlockName());
        }
        element.setAttribute("slotOffset", String.valueOf(lt.getSlotOffset()));
        element.setAttribute("orientation", String.valueOf(lt.getOrientation()));

        Point2D coords = pv.getCoordsCenter();
        element.setAttribute("xcen", "" + coords.getX());
        element.setAttribute("ycen", "" + coords.getY());
        element.setAttribute("turnoutControlled", "" + (turnoutControl ? "yes" : "no"));
        boolean dispatcherManaged = lt.isDispatcherManaged();
        if (dispatcherManaged) {
            String exitMastName = lt.getExitSignalMastName();
            if (exitMastName != null && !exitMastName.isEmpty()) {
                element.setAttribute("exitmast", exitMastName);
            }
            String bufferMastName = lt.getBufferSignalMastName();
            if (bufferMastName != null && !bufferMastName.isEmpty()) {
                element.setAttribute("buffermast", bufferMastName);
            }
            element.setAttribute("signalIconPlacement", "" + lt.getSignalIconPlacement());
            element.setAttribute("dispatcherManaged", "yes");
        }
        element.setAttribute("class", "jmri.jmrit.display.layoutEditor.configurexml.LayoutTraverserViewXml");
        // add slot tracks
        for (int i = 0; i < lt.getNumberSlots(); i++) {
            Element rElem = new Element("slot");
            rElem.setAttribute("offset", "" + lt.getSlotOffsetValue(i));
            TrackSegment t = lt.getSlotConnectOrdered(i);
            if (t != null) {
                rElem.setAttribute("connectname", t.getId());
            }
            String mastName = lt.getSlotList().get(i).getApproachMastName();
            if (mastName != null && !mastName.isEmpty()) {
                rElem.setAttribute("approachmast", mastName);
            }
            rElem.setAttribute("index", "" + lt.getSlotIndex(i));
            if (turnoutControl && lt.getSlotTurnoutName(i) != null) {
                rElem.setAttribute("turnout", lt.getSlotTurnoutName(i));
                if (lt.getSlotTurnoutState(i) == Turnout.THROWN) {
                    rElem.setAttribute("turnoutstate", "thrown");
                } else {
                    rElem.setAttribute("turnoutstate", "closed");
                }
                if (lt.isSlotDisabled(i)) {
                    rElem.setAttribute("disabled", "yes");
                }
                if (lt.isSlotDisabledWhenOccupied(i)) {
                    rElem.setAttribute("disableWhenOccupied", "yes");
                }
            }
            element.addContent(rElem);
        }
        storeLogixNG_Data(pv, element);
        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the layout traverser element, then all the other data
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
        double slotOffset = 25.0;
        int orientation = LayoutTraverser.HORIZONTAL;
        try {
            x = element.getAttribute("xcen").getFloatValue();
            y = element.getAttribute("ycen").getFloatValue();
            if (element.getAttribute("slotOffset") != null) {
                slotOffset = element.getAttribute("slotOffset").getDoubleValue();
            }
            if (element.getAttribute("orientation") != null) {
                orientation = element.getAttribute("orientation").getIntValue();
            }
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert layouttraverser attributes", e);
        }
        // create the new LayoutTraverser
        LayoutTraverser lt = new LayoutTraverser(name, p);
        LayoutTraverserView lv = new LayoutTraverserView(lt, new Point2D.Double(x, y), p);

        p.addLayoutTrack(lv.getTraverser(), lv);

        lv.setCoordsCenter(new Point2D.Double(x, y));
        lt.setSlotOffset(slotOffset);
        lt.setOrientation(orientation);

        // get remaining attribute
        Attribute a = element.getAttribute("blockname");
        if (a != null) {
            lt.tLayoutBlockName = a.getValue();
        }

        a = element.getAttribute("turnoutControlled");
        if (a != null) {
            lt.setTurnoutControlled("yes".equalsIgnoreCase(a.getValue()));
        }

        a = element.getAttribute("dispatcherManaged");
        if (a != null) {
            lt.setDispatcherManaged("yes".equalsIgnoreCase(a.getValue()));
            if (lt.isDispatcherManaged()) {
                a = element.getAttribute("exitmast");
                if (a != null) {
                    lt.tExitSignalMastName = a.getValue();
                }
                a = element.getAttribute("buffermast");
                if (a != null) {
                    lt.tBufferSignalMastName = a.getValue();
                }
                a = element.getAttribute("signalIconPlacement");
                if (a != null) {
                    try {
                        lt.setSignalIconPlacement(a.getIntValue());
                    } catch (DataConversionException e) {
                        log.error("failed to convert signalIconPlacement attribute");
                    }
                }
            }
        }

        // load slot tracks
        List<Element> slotTrackList = element.getChildren("slot");
        if (slotTrackList.size() > 0) {
            for (Element value : slotTrackList) {
                double offset = 0.0;
                int index = 0;
                try {
                    offset = (value.getAttribute("offset")).getFloatValue();
                    index = (value.getAttribute("index")).getIntValue();
                } catch (DataConversionException e) {
                    log.error("failed to convert slot track offset or index attributes");
                }
                String connectName = "";
                a = value.getAttribute("connectname");
                if (a != null) {
                    connectName = a.getValue();
                }
                lt.addSlotTrack(offset, index, connectName);
                a = value.getAttribute("approachmast");
                if (a != null) {
                    lt.getSlotList().get(lt.getNumberSlots() - 1).approachMastName = a.getValue();
                }
                a = value.getAttribute("turnout");
                if (lt.isTurnoutControlled() && a != null) {
                    if (value.getAttribute("turnoutstate").getValue().equals("thrown")) {
                        lt.setSlotTurnout(index, value.getAttribute("turnout").getValue(), Turnout.THROWN);
                    } else {
                        lt.setSlotTurnout(index, value.getAttribute("turnout").getValue(), Turnout.CLOSED);
                    }
                    a = value.getAttribute("disabled");
                    if (a != null) {
                        lt.setSlotDisabled(index, "yes".equalsIgnoreCase(a.getValue()));
                    }
                    a = value.getAttribute("disableWhenOccupied");
                    if (a != null) {
                        lt.setSlotDisabledWhenOccupied(index, "yes".equalsIgnoreCase(a.getValue()));
                    }
                }
            }
        }

        loadLogixNG_Data(lv, element);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTraverserViewXml.class);
}
