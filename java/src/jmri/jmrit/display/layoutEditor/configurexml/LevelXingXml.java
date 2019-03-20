package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.geom.Point2D;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This module handles configuration for display.LevelXing objects for a
 * LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2019
 */
public class LevelXingXml extends AbstractXmlAdapter {

    public LevelXingXml() {
    }

    /**
     * Default implementation for storing the contents of a LevelXing
     *
     * @param o Object to store, of type LevelXing
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        LevelXing p = (LevelXing) o;

        Element element = new Element("levelxing");

        // include attributes
        element.setAttribute("ident", p.getId());
        if (!p.getBlockNameAC().isEmpty()) {
            element.setAttribute("blocknameac", p.getBlockNameAC());
        }
        if (!p.getBlockNameBD().isEmpty()) {
            element.setAttribute("blocknamebd", p.getBlockNameBD());
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
        if (p.isHidden()) {
            element.setAttribute("hidden", "yes");
        }
        if (!p.getSignalAName().isEmpty()) {
            element.setAttribute("signalaname", p.getSignalAName());
        }
        if (!p.getSignalBName().isEmpty()) {
            element.setAttribute("signalbname", p.getSignalBName());
        }
        if (!p.getSignalCName().isEmpty()) {
            element.setAttribute("signalcname", p.getSignalCName());
        }
        if (!p.getSignalDName().isEmpty()) {
            element.setAttribute("signaldname", p.getSignalDName());
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
            log.error("failed to convert levelxing center  attribute");
        }

        // create the new LevelXing
        LevelXing l = new LevelXing(name, new Point2D.Double(x, y), p);

        // get remaining attributes
        Attribute a = element.getAttribute("blocknameac");
        if (a != null) {
            l.tLayoutBlockNameAC = a.getValue();
        }
        a = element.getAttribute("blocknamebd");
        if (a != null) {
            l.tLayoutBlockNameBD = a.getValue();
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
        a = element.getAttribute("signalaname");
        if (a != null) {
            l.setSignalAName(a.getValue());
        }
        a = element.getAttribute("signalbname");
        if (a != null) {
            l.setSignalBName(a.getValue());
        }
        a = element.getAttribute("signalcname");
        if (a != null) {
            l.setSignalCName(a.getValue());
        }
        a = element.getAttribute("signaldname");
        if (a != null) {
            l.setSignalDName(a.getValue());
        }

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

        try {
            l.setHidden(element.getAttribute("hidden").getBooleanValue());
        } catch (DataConversionException e1) {
            log.warn("unable to convert levelxing hidden attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
        }

        if (element.getChild("signalAMast") != null) {
            String mast = element.getChild("signalAMast").getText();
            if (mast != null && !mast.isEmpty()) {
                l.setSignalAMast(mast);
            }
        }

        if (element.getChild("signalBMast") != null) {
            String mast = element.getChild("signalBMast").getText();
            if (mast != null && !mast.isEmpty()) {
                l.setSignalBMast(mast);
            }
        }

        if (element.getChild("signalCMast") != null) {
            String mast = element.getChild("signalCMast").getText();
            if (mast != null && !mast.isEmpty()) {
                l.setSignalCMast(mast);
            }
        }

        if (element.getChild("signalDMast") != null) {
            String mast = element.getChild("signalDMast").getText();
            if (mast != null && !mast.isEmpty()) {
                l.setSignalDMast(mast);
            }
        }

        if (element.getChild("sensorA") != null) {
            String sensor = element.getChild("sensorA").getText();
            if (sensor != null && !sensor.isEmpty()) {
                l.setSensorAName(sensor);
            }
        }

        if (element.getChild("sensorB") != null) {
            String sensor = element.getChild("sensorB").getText();
            if (sensor != null && !sensor.isEmpty()) {
                l.setSensorBName(sensor);
            }
        }

        if (element.getChild("sensorC") != null) {
            String sensor = element.getChild("sensorC").getText();
            if (sensor != null && !sensor.isEmpty()) {
                l.setSensorCName(sensor);
            }
        }

        if (element.getChild("sensorD") != null) {
            String sensor = element.getChild("sensorD").getText();
            if (sensor != null && !sensor.isEmpty()) {
                l.setSensorDName(sensor);
            }
        }

        p.getLayoutTracks().add(l);
    }

    private final static Logger log = LoggerFactory.getLogger(LevelXingXml.class);
}
