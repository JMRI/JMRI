package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.geom.Point2D;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module handles configuration for display.LayoutSlip objects for a
 * LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
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
    public Element store(Object o) {

        LayoutSlip p = (LayoutSlip) o;

        Element element = new Element("layoutSlip");

        // include attributes
        element.setAttribute("ident", p.getName());

        element.setAttribute("slipType", "" + p.getSlipType());
        if (p.getBlockName().length() > 0) {
            element.setAttribute("blockname", p.getBlockName());
        }

        if (p.getConnectA() != null) {
            element.setAttribute("connectaname", ((TrackSegment) p.getConnectA()).getID());
        }
        if (p.getConnectB() != null) {
            element.setAttribute("connectbname", ((TrackSegment) p.getConnectB()).getID());
        }
        if (p.getConnectC() != null) {
            element.setAttribute("connectcname", ((TrackSegment) p.getConnectC()).getID());
        }
        if (p.getConnectD() != null) {
            element.setAttribute("connectdname", ((TrackSegment) p.getConnectD()).getID());
        }
        if (p.getSignalA1Name().length() > 0) {
            element.addContent(new Element("signala1name").addContent(p.getSignalA1Name()));
        }
        if (p.getSignalB1Name().length() > 0) {
            element.addContent(new Element("signalb1name").addContent(p.getSignalB1Name()));
        }
        if (p.getSignalC1Name().length() > 0) {
            element.addContent(new Element("signalc1name").addContent(p.getSignalC1Name()));
        }
        if (p.getSignalD1Name().length() > 0) {
            element.addContent(new Element("signald1name").addContent(p.getSignalD1Name()));
        }
        if (p.getSignalA2Name().length() > 0) {
            element.addContent(new Element("signala2name").addContent(p.getSignalA2Name()));
        }
        if (p.getSignalB2Name().length() > 0) {
            element.addContent(new Element("signalb2name").addContent(p.getSignalB2Name()));
        }
        if (p.getSignalC2Name().length() > 0) {
            element.addContent(new Element("signalc2name").addContent(p.getSignalC2Name()));
        }
        if (p.getSignalD2Name().length() > 0) {
            element.addContent(new Element("signald2name").addContent(p.getSignalD2Name()));
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

        if (p.getSignalAMastName().length() > 0) {
            element.addContent(new Element("signalAMast").addContent(p.getSignalAMastName()));
        }

        if (p.getSignalBMastName().length() > 0) {
            element.addContent(new Element("signalBMast").addContent(p.getSignalBMastName()));
        }
        if (p.getSignalCMastName().length() > 0) {
            element.addContent(new Element("signalCMast").addContent(p.getSignalCMastName()));
        }
        if (p.getSignalDMastName().length() > 0) {
            element.addContent(new Element("signalDMast").addContent(p.getSignalDMastName()));
        }

        if (p.getSensorAName().length() > 0) {
            element.addContent(new Element("sensorA").addContent(p.getSensorAName()));
        }

        if (p.getSensorBName().length() > 0) {
            element.addContent(new Element("sensorB").addContent(p.getSensorBName()));
        }
        if (p.getSensorCName().length() > 0) {
            element.addContent(new Element("sensorC").addContent(p.getSensorCName()));
        }
        if (p.getSensorDName().length() > 0) {
            element.addContent(new Element("sensorD").addContent(p.getSensorDName()));
        }

        if (p.getTurnoutName().length() > 0) {
            element.addContent(new Element("turnout").addContent(p.getTurnoutName()));
        }

        if (p.getTurnoutBName().length() > 0) {
            element.addContent(new Element("turnoutB").addContent(p.getTurnoutBName()));
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
     * Load, starting with the levelxing element, then all the other data
     *
     * @param element Top level Element to unpack.
     * @param o       LayoutEditor as an Object
     */
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
        }

        // create the new LayoutSlip
        LayoutSlip l = new LayoutSlip(name, new Point2D.Double(x, y), 0.0, p, type);

        // get remaining attributes
        Attribute a = element.getAttribute("blockname");
        if (a != null) {
            l.tBlockName = a.getValue();
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
            x = element.getAttribute("xa").getFloatValue();
            y = element.getAttribute("ya").getFloatValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert levelxing a coords attribute");
        }
        l.setCoordsA(new Point2D.Double(x, y));
        try {
            x = element.getAttribute("xb").getFloatValue();
            y = element.getAttribute("yb").getFloatValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert levelxing b coords attribute");
        }
        l.setCoordsB(new Point2D.Double(x, y));

        l.setSignalAMast(getElement(element, "signalAMast"));
        l.setSignalBMast(getElement(element, "signalBMast"));
        l.setSignalCMast(getElement(element, "signalCMast"));
        l.setSignalDMast(getElement(element, "signalDMast"));

        l.setSensorA(getElement(element, "sensorA"));
        l.setSensorB(getElement(element, "sensorB"));
        l.setSensorC(getElement(element, "sensorC"));
        l.setSensorD(getElement(element, "sensorD"));

        l.setTurnout(getElement(element, "turnout"));
        l.setTurnoutB(getElement(element, "turnoutB"));

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
        p.slipList.add(l);
    }

    String getElement(Element el, String child) {
        if (el.getChild(child) != null) {
            return el.getChild(child).getText();
        }
        return "";
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutSlipXml.class.getName());
}
