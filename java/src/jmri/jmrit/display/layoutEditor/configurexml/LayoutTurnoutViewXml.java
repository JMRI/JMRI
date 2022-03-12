package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.geom.Point2D;

import jmri.Turnout;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.*;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module handles configuration for display.LayoutTurnout objects for a
 * LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2019
 * @author Bob Jacobsen Copyright (c) 2020
 */
public class LayoutTurnoutViewXml extends AbstractXmlAdapter {

    static final EnumIO<LayoutTurnout.LinkType> linkEnumMap = new EnumIoNamesNumbers<>(LayoutTurnout.LinkType.class);
    static final EnumIO<LayoutTurnout.TurnoutType> tTypeEnumMap = new EnumIoNamesNumbers<>(LayoutTurnout.TurnoutType.class);
    
    public LayoutTurnoutViewXml() {
    }

    protected void addClass(Element element) {
        element.setAttribute("class", "jmri.jmrit.display.layoutEditor.configurexml.LayoutTurnoutXml");
    }

    /**
     * Default implementation for storing the contents of a LayoutTurnout
     *
     * @param o Object to store, of type LayoutTurnout
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        LayoutTurnoutView pv = (LayoutTurnoutView) o;
        LayoutTurnout p = pv.getLayoutTurnout();

        Element element = new Element("layoutturnout");

        // include attributes
        element.setAttribute("ident", p.getName());
        element.setAttribute("type", tTypeEnumMap.outputFromEnum(p.getTurnoutType()));

        element.setAttribute("hidden", "" + (pv.isHidden() ? "yes" : "no"));
        element.setAttribute("disabled", "" + (p.isDisabled() ? "yes" : "no"));
        element.setAttribute("disableWhenOccupied", "" + (p.isDisabledWhenOccupied() ? "yes" : "no"));

        element.setAttribute("continuing", "" + p.getContinuingSense());

        Point2D coords = pv.getCoordsCenter();
        element.setAttribute("xcen", "" + coords.getX());
        element.setAttribute("ycen", "" + coords.getY());
        
        coords = pv.getCoordsA();
        element.setAttribute("xa", "" + coords.getX());
        element.setAttribute("ya", "" + coords.getY());
        
        coords = pv.getCoordsB();
        element.setAttribute("xb", "" + coords.getX());
        element.setAttribute("yb", "" + coords.getY());
        
        coords = pv.getCoordsC();
        element.setAttribute("xc", "" + coords.getX());
        element.setAttribute("yc", "" + coords.getY());
        
        coords = pv.getCoordsD();
        element.setAttribute("xd", "" + coords.getX());
        element.setAttribute("yd", "" + coords.getY());
        
        element.setAttribute("ver", "" + p.getVersion());
        
        addClass(element);

        if (!p.getTurnoutName().isEmpty()) {
            element.setAttribute("turnoutname", p.getTurnoutName());
        }
        if (!p.getSecondTurnoutName().isEmpty()) {
            element.setAttribute("secondturnoutname", p.getSecondTurnoutName());
            if (p.isSecondTurnoutInverted()) {
                element.setAttribute("secondturnoutinverted", "true");
            }
        }

        if (!p.getLinkedTurnoutName().isEmpty()) {
            element.setAttribute("linkedturnoutname", p.getLinkedTurnoutName());
            element.setAttribute("linktype", "" + linkEnumMap.outputFromEnum(p.getLinkType()));
        }

        if (!p.getBlockName().isEmpty()) {
            element.setAttribute("blockname", p.getBlockName());
        }
        // Only save these if they're different from block A
        if (!p.getBlockBName().isEmpty() && (!p.getBlockBName().equals(p.getBlockName()))) {
            element.setAttribute("blockbname", p.getBlockBName());
        }
        if (!p.getBlockCName().isEmpty() && (!p.getBlockCName().equals(p.getBlockName()))) {
            element.setAttribute("blockcname", p.getBlockCName());
        }
        if (!p.getBlockDName().isEmpty() && (!p.getBlockDName().equals(p.getBlockName()))) {
            element.setAttribute("blockdname", p.getBlockDName());
        }

        if (p.getConnectA() != null) {
            element.setAttribute("connectaname", ((TrackSegment) p.getConnectA()).getId());
        }
        if (p.getConnectB() != null) {
            element.setAttribute("connectbname", ((TrackSegment) p.getConnectB()).getId());
        }
        if (p.getConnectC() != null) {
            element.setAttribute("connectcname", ((TrackSegment) p.getConnectC()).getId());
        }
        if (p.getConnectD() != null) {
            element.setAttribute("connectdname", ((TrackSegment) p.getConnectD()).getId());
        }

        if (!p.getSignalA1Name().isEmpty()) {
            element.setAttribute("signala1name", p.getSignalA1Name());
        }
        if (!p.getSignalA2Name().isEmpty()) {
            element.setAttribute("signala2name", p.getSignalA2Name());
        }
        if (!p.getSignalA3Name().isEmpty()) {
            element.setAttribute("signala3name", p.getSignalA3Name());
        }
        if (!p.getSignalB1Name().isEmpty()) {
            element.setAttribute("signalb1name", p.getSignalB1Name());
        }
        if (!p.getSignalB2Name().isEmpty()) {
            element.setAttribute("signalb2name", p.getSignalB2Name());
        }
        if (!p.getSignalC1Name().isEmpty()) {
            element.setAttribute("signalc1name", p.getSignalC1Name());
        }
        if (!p.getSignalC2Name().isEmpty()) {
            element.setAttribute("signalc2name", p.getSignalC2Name());
        }
        if (!p.getSignalD1Name().isEmpty()) {
            element.setAttribute("signald1name", p.getSignalD1Name());
        }
        if (!p.getSignalD2Name().isEmpty()) {
            element.setAttribute("signald2name", p.getSignalD2Name());
        }

        if (!p.getSignalAMastName().isEmpty()) {
            element.addContent(new Element("signalAMast").addContent(p.getSignalAMastName()));
        }

        if (!p.getSignalBMastName().isEmpty()) {
            element.addContent(new Element("signalBMast").addContent(p.getSignalBMastName()));
        }
        if (!p.getSignalCMastName().isEmpty()) {
            element.addContent(new Element("signalCMast").addContent(p.getSignalCMastName()));
        }
        if (!p.getSignalDMastName().isEmpty()) {
            element.addContent(new Element("signalDMast").addContent(p.getSignalDMastName()));
        }

        if (!p.getSensorAName().isEmpty()) {
            element.addContent(new Element("sensorA").addContent(p.getSensorAName()));
        }

        if (!p.getSensorBName().isEmpty()) {
            element.addContent(new Element("sensorB").addContent(p.getSensorBName()));
        }
        if (!p.getSensorCName().isEmpty()) {
            element.addContent(new Element("sensorC").addContent(p.getSensorCName()));
        }
        if (!p.getSensorDName().isEmpty()) {
            element.addContent(new Element("sensorD").addContent(p.getSensorDName()));
        }
        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the levelxing element, then all the other data
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
        LayoutTurnout.TurnoutType type = LayoutTurnout.TurnoutType.NONE;
        try {
            x = element.getAttribute("xcen").getFloatValue();
            y = element.getAttribute("ycen").getFloatValue();
            type = tTypeEnumMap.inputFromAttribute(element.getAttribute("type"));
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert layoutturnout attribute");
        }

        int version = 1;
        try {
            if (element.getAttribute("ver") != null) {
                version = element.getAttribute("ver").getIntValue();
            }
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert layoutturnout version attribute");
        }

        // create the new LayoutTurnout of the correct type
        LayoutTurnoutView lv; 
        LayoutTurnout l;
        
        switch(type) {

            case RH_TURNOUT :
                LayoutRHTurnout lrht = new LayoutRHTurnout(name, p, version); 
                l = lrht;
                lv = new LayoutRHTurnoutView(lrht, new Point2D.Double(x, y), 0.0, 1.0, 1.0, p);
                break;
            case LH_TURNOUT :
                LayoutLHTurnout llht = new LayoutLHTurnout(name, p, version);
                l = llht;
                lv = new LayoutLHTurnoutView(llht, new Point2D.Double(x, y), 0.0, 1.0, 1.0, p);
                break;
            case WYE_TURNOUT :
                LayoutWye lwt = new LayoutWye(name, p, version);
                l = lwt;
                lv = new LayoutWyeView(lwt, new Point2D.Double(x, y), 0.0, 1.0, 1.0, p);
                break;
            case DOUBLE_XOVER :
                LayoutDoubleXOver ldx = new LayoutDoubleXOver(name, p, version);
                l = ldx;
                lv = new LayoutDoubleXOverView(ldx, new Point2D.Double(x, y), 0.0, 1.0, 1.0, p);
                break;
            case RH_XOVER :
                LayoutRHXOver lrx = new LayoutRHXOver(name, p, version);
                l = lrx;
                lv = new LayoutRHXOverView(lrx, new Point2D.Double(x, y), 0.0, 1.0, 1.0, p);
                break;
            case LH_XOVER :
                LayoutLHXOver llx = new LayoutLHXOver(name, p, version);
                l = llx;
                lv = new LayoutLHXOverView(llx, new Point2D.Double(x, y), 0.0, 1.0, 1.0, p);
                break;

            case DOUBLE_SLIP :
                LayoutDoubleSlip lds = new LayoutDoubleSlip(name, p);
                l = lds;
                lv = new LayoutDoubleSlipView(lds, new Point2D.Double(x, y), 0.0, p);
                log.error("Found DOUBLE_SLIP in LayoutTrack ctor for element {}", name);
                break;
            case SINGLE_SLIP :
                LayoutSingleSlip lss = new LayoutSingleSlip(name, p);
                l = lss;
                lv = new LayoutSingleSlipView(lss, new Point2D.Double(x, y), 0.0, p);
                log.error("Found SINGLE_SLIP in LayoutTrack ctor for element {}", name);
                break;

            default:
                log.error("can't create LayoutTrack {} with type {}", name, type);
                return; // without creating
        }

        p.addLayoutTrack(l, lv);

        // get remaining attributes
        Attribute a = element.getAttribute("turnoutname");
        if (a != null) {
            l.setTurnout(a.getValue());
        }
        a = element.getAttribute("secondturnoutname");
        if (a != null) {
            l.setSecondTurnout(a.getValue());
            try {
                l.setSecondTurnoutInverted(element.getAttribute("secondturnoutinverted").getBooleanValue());
            } catch (DataConversionException e1) {
                log.warn("unable to convert layout turnout secondturnoutinverted attribute");
            } catch (NullPointerException e) {  // considered normal if the attribute is not present
            }
        }

        a = element.getAttribute("blockname");
        if (a != null) {
            l.tBlockAName = a.getValue();
        }
        a = element.getAttribute("blockbname");
        if (a != null) {
            l.tBlockBName = a.getValue();
        }
        a = element.getAttribute("blockcname");
        if (a != null) {
            l.tBlockCName = a.getValue();
        }
        a = element.getAttribute("blockdname");
        if (a != null) {
            l.tBlockDName = a.getValue();
        }

        a = element.getAttribute("connectaname");
        if (a != null) {
            l.connectAName = a.getValue();
        }
        a = element.getAttribute("connectbname");
        if (a != null) {
            l.connectBName = a.getValue();
        }
        a = element.getAttribute("connectcname");
        if (a != null) {
            l.connectCName = a.getValue();
        }
        a = element.getAttribute("connectdname");
        if (a != null) {
            l.connectDName = a.getValue();
        }

        a = element.getAttribute("signala1name");
        if (a != null) {
            l.setSignalA1Name(a.getValue());
        }
        a = element.getAttribute("signala2name");
        if (a != null) {
            l.setSignalA2Name(a.getValue());
        }
        a = element.getAttribute("signala3name");
        if (a != null) {
            l.setSignalA3Name(a.getValue());
        }
        a = element.getAttribute("signalb1name");
        if (a != null) {
            l.setSignalB1Name(a.getValue());
        }
        a = element.getAttribute("signalb2name");
        if (a != null) {
            l.setSignalB2Name(a.getValue());
        }
        a = element.getAttribute("signalc1name");
        if (a != null) {
            l.setSignalC1Name(a.getValue());
        }
        a = element.getAttribute("signalc2name");
        if (a != null) {
            l.setSignalC2Name(a.getValue());
        }
        a = element.getAttribute("signald1name");
        if (a != null) {
            l.setSignalD1Name(a.getValue());
        }
        a = element.getAttribute("signald2name");
        if (a != null) {
            l.setSignalD2Name(a.getValue());
        }
        a = element.getAttribute("linkedturnoutname");
        if (a != null) {
            l.linkedTurnoutName = a.getValue();
            l.linkType = linkEnumMap.inputFromAttribute(element.getAttribute("linktype"));
        }
        a = element.getAttribute("continuing");
        if (a != null) {
            int continuing = Turnout.CLOSED;
            try {
                continuing = element.getAttribute("continuing").getIntValue();
            } catch (org.jdom2.DataConversionException e) {
                log.error("failed to convert continuingsense attribute");
            }
            l.setContinuingSense(continuing);
        }
        try {
            l.setDisabled(element.getAttribute("disabled").getBooleanValue());
        } catch (DataConversionException e1) {
            log.warn("unable to convert layout turnout disabled attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
        }
        try {
            l.setDisableWhenOccupied(element.getAttribute("disableWhenOccupied").getBooleanValue());
        } catch (DataConversionException e1) {
            log.warn("unable to convert layout turnout disableWhenOccupied attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
        }
        try {
            lv.setHidden(element.getAttribute("hidden").getBooleanValue());
        } catch (DataConversionException e1) {
            log.warn("unable to convert layout turnout hidden attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
        }

        if (version == 2) {
            try {
                x = element.getAttribute("xa").getFloatValue();
                y = element.getAttribute("ya").getFloatValue();
                lv.setCoordsA(new Point2D.Double(x, y));
            } catch (org.jdom2.DataConversionException e) {
                log.error("failed to convert layoutturnout b coords attribute");
            } catch (java.lang.NullPointerException e) {
                //can be ignored as panel file may not support method
            }
        }
        try {
            x = element.getAttribute("xb").getFloatValue();
            y = element.getAttribute("yb").getFloatValue();
            lv.setCoordsB(new Point2D.Double(x, y));
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert layoutturnout b coords attribute");
        }
        try {
            x = element.getAttribute("xc").getFloatValue();
            y = element.getAttribute("yc").getFloatValue();
            lv.setCoordsC(new Point2D.Double(x, y));
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert layoutturnout c coords attribute");
        }
        if (version == 2) {
            try {
                x = element.getAttribute("xd").getFloatValue();
                y = element.getAttribute("yd").getFloatValue();
                lv.setCoordsD(new Point2D.Double(x, y));
            } catch (org.jdom2.DataConversionException e) {
                log.error("failed to convert layoutturnout c coords attribute");
            } catch (java.lang.NullPointerException e) {
                //can be ignored as panel file may not support method
            }
        }

        l.setSignalAMast(getElement(element, "signalAMast"));
        l.setSignalBMast(getElement(element, "signalBMast"));
        l.setSignalCMast(getElement(element, "signalCMast"));
        l.setSignalDMast(getElement(element, "signalDMast"));

        l.setSensorA(getElement(element, "sensorA"));
        l.setSensorB(getElement(element, "sensorB"));
        l.setSensorC(getElement(element, "sensorC"));
        l.setSensorD(getElement(element, "sensorD"));
    }

    String getElement(Element el, String child) {
        if (el.getChild(child) != null) {
            return el.getChild(child).getText();
        }
        return "";
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutTurnoutViewXml.class);
}
