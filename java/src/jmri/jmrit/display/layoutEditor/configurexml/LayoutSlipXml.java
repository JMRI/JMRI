package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.geom.Point2D;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module handles configuration for display.LayoutSlip objects for a
 * LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutSlipXml extends AbstractXmlAdapter {

    public LayoutSlipXml() {
    }

    /**
     * Default implementation for storing the contents of a LayoutSlip
     *
     * @param o Object to store, of type LayoutSlip
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        LayoutSlip p = (LayoutSlip) o;

        Element element = new Element("layoutSlip");

        // include attributes
        element.setAttribute("ident", p.getName());
        element.setAttribute("slipType", "" + p.getSlipType());

        element.setAttribute("hidden", "" + (p.isHidden() ? "yes" : "no"));
        element.setAttribute("disabled", "" + (p.isDisabled() ? "yes" : "no"));
        element.setAttribute("disableWhenOccupied", "" + (p.isDisabledWhenOccupied() ? "yes" : "no"));

        Point2D coords = p.getCoordsCenter();
        element.setAttribute("xcen", "" + coords.getX());
        element.setAttribute("ycen", "" + coords.getY());
        coords = p.getCoordsA();
        element.setAttribute("xa", "" + coords.getX());
        element.setAttribute("ya", "" + coords.getY());
        coords = p.getCoordsB();
        element.setAttribute("xb", "" + coords.getX());
        element.setAttribute("yb", "" + coords.getY());

        if (!p.getTurnoutName().isEmpty()) {
            element.addContent(new Element("turnout").addContent(p.getTurnoutName()));
        }

        if (!p.getTurnoutBName().isEmpty()) {
            element.addContent(new Element("turnoutB").addContent(p.getTurnoutBName()));
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
            element.addContent(new Element("signala1name").addContent(p.getSignalA1Name()));
        }
        if (!p.getSignalB1Name().isEmpty()) {
            element.addContent(new Element("signalb1name").addContent(p.getSignalB1Name()));
        }
        if (!p.getSignalC1Name().isEmpty()) {
            element.addContent(new Element("signalc1name").addContent(p.getSignalC1Name()));
        }
        if (!p.getSignalD1Name().isEmpty()) {
            element.addContent(new Element("signald1name").addContent(p.getSignalD1Name()));
        }
        if (!p.getSignalA2Name().isEmpty()) {
            element.addContent(new Element("signala2name").addContent(p.getSignalA2Name()));
        }
        if (!p.getSignalB2Name().isEmpty()) {
            element.addContent(new Element("signalb2name").addContent(p.getSignalB2Name()));
        }
        if (!p.getSignalC2Name().isEmpty()) {
            element.addContent(new Element("signalc2name").addContent(p.getSignalC2Name()));
        }
        if (!p.getSignalD2Name().isEmpty()) {
            element.addContent(new Element("signald2name").addContent(p.getSignalD2Name()));
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

        Element states = new Element("states");
        Element state = new Element("A-C");
        state.addContent(new Element("turnout").addContent("" + p.getTurnoutState(LayoutSlip.STATE_AC)));
        state.addContent(new Element("turnoutB").addContent("" + p.getTurnoutBState(LayoutSlip.STATE_AC)));
        states.addContent(state);

        state = new Element("A-D");
        state.addContent(new Element("turnout").addContent("" + p.getTurnoutState(LayoutSlip.STATE_AD)));
        state.addContent(new Element("turnoutB").addContent("" + p.getTurnoutBState(LayoutSlip.STATE_AD)));
        states.addContent(state);

        state = new Element("B-D");
        state.addContent(new Element("turnout").addContent("" + p.getTurnoutState(LayoutSlip.STATE_BD)));
        state.addContent(new Element("turnoutB").addContent("" + p.getTurnoutBState(LayoutSlip.STATE_BD)));
        states.addContent(state);

        if (p.getSlipType() == LayoutSlip.DOUBLE_SLIP) {
            state = new Element("B-C");
            state.addContent(new Element("turnout").addContent("" + p.getTurnoutState(LayoutSlip.STATE_BC)));
            state.addContent(new Element("turnoutB").addContent("" + p.getTurnoutBState(LayoutSlip.STATE_BC)));
            states.addContent(state);
        }
        element.addContent(states);
        element.setAttribute("class", getClass().getName());
        return element;
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the LayoutSlip element, then all the other data
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
            log.error("failed to convert layoutslip center  attribute");
        }
        int type = LayoutSlip.SINGLE_SLIP;
        try {
            type = element.getAttribute("slipType").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert layoutslip type attribute");
        } catch (java.lang.NullPointerException e) {
            //can be ignored as panel file may not support method
        }

        // create the new LayoutSlip
        LayoutSlip l = new LayoutSlip(name, new Point2D.Double(x, y), 0.0, p, type);

        // get remaining attributes
        l.setTurnout(getElement(element, "turnout"));
        l.setTurnoutB(getElement(element, "turnoutB"));

        Attribute a = element.getAttribute("blockname");
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

        l.setSignalA1Name(getElement(element, "signala1name"));
        l.setSignalB1Name(getElement(element, "signalb1name"));
        l.setSignalC1Name(getElement(element, "signalc1name"));
        l.setSignalD1Name(getElement(element, "signald1name"));

        l.setSignalA2Name(getElement(element, "signala2name"));
        l.setSignalB2Name(getElement(element, "signalb2name"));
        l.setSignalC2Name(getElement(element, "signalc2name"));
        l.setSignalD2Name(getElement(element, "signald2name"));

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
            l.setHidden(element.getAttribute("hidden").getBooleanValue());
        } catch (DataConversionException e1) {
            log.warn("unable to convert layout turnout hidden attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
        }

        try {
            x = element.getAttribute("xa").getFloatValue();
            y = element.getAttribute("ya").getFloatValue();
            l.setCoordsA(new Point2D.Double(x, y));
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert LayoutSlip a coords attribute");
        }
        try {
            x = element.getAttribute("xb").getFloatValue();
            y = element.getAttribute("yb").getFloatValue();
            l.setCoordsB(new Point2D.Double(x, y));
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert LayoutSlip b coords attribute");
        }

        l.setSignalAMast(getElement(element, "signalAMast"));
        l.setSignalBMast(getElement(element, "signalBMast"));
        l.setSignalCMast(getElement(element, "signalCMast"));
        l.setSignalDMast(getElement(element, "signalDMast"));

        l.setSensorA(getElement(element, "sensorA"));
        l.setSensorB(getElement(element, "sensorB"));
        l.setSensorC(getElement(element, "sensorC"));
        l.setSensorD(getElement(element, "sensorD"));

        if (element.getChild("states") != null) {
            Element state = element.getChild("states");
            if (state.getChild("A-C") != null) {
                Element ac = state.getChild("A-C");
                l.setTurnoutStates(LayoutSlip.STATE_AC,
                        ac.getChild("turnout").getText(),
                        ac.getChild("turnoutB").getText());
            }
            if (state.getChild("A-D") != null) {
                Element ad = state.getChild("A-D");
                l.setTurnoutStates(LayoutSlip.STATE_AD,
                        ad.getChild("turnout").getText(),
                        ad.getChild("turnoutB").getText());
            }
            if (state.getChild("B-D") != null) {
                Element bd = state.getChild("B-D");
                l.setTurnoutStates(LayoutSlip.STATE_BD,
                        bd.getChild("turnout").getText(),
                        bd.getChild("turnoutB").getText());
            }
            if (state.getChild("B-C") != null) {
                Element bc = state.getChild("B-C");
                l.setTurnoutStates(LayoutSlip.STATE_BC,
                        bc.getChild("turnout").getText(),
                        bc.getChild("turnoutB").getText());
            }
        }
        p.getLayoutTracks().add(l);
    }

    String getElement(Element el, String child) {
        if (el.getChild(child) != null) {
            return el.getChild(child).getText();
        }
        return "";
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutSlipXml.class);
}
