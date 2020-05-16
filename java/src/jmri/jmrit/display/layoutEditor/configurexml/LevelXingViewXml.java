package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.geom.Point2D;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.LevelXingView;
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
public class LevelXingViewXml extends AbstractXmlAdapter {

    public LevelXingViewXml() {
    }

    /**
     * Default implementation for storing the contents of a LevelXing
     *
     * @param o Object to store, of type LevelXing
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {

        
        LevelXingView lv = (LevelXingView) o;
        LevelXing lt = lv.getLevelXing();
        
        Element element = new Element("levelxing");

        // include attributes
        element.setAttribute("ident", lt.getId());
        if (!lt.getBlockNameAC().isEmpty()) {
            element.setAttribute("blocknameac", lt.getBlockNameAC());
        }
        if (!lt.getBlockNameBD().isEmpty()) {
            element.setAttribute("blocknamebd", lt.getBlockNameBD());
        }
        if (lt.getConnectA() != null) {
            element.setAttribute("connectaname", ((TrackSegment) lt.getConnectA()).getId());
        }
        if (lt.getConnectB() != null) {
            element.setAttribute("connectbname", ((TrackSegment) lt.getConnectB()).getId());
        }
        if (lt.getConnectC() != null) {
            element.setAttribute("connectcname", ((TrackSegment) lt.getConnectC()).getId());
        }
        if (lt.getConnectD() != null) {
            element.setAttribute("connectdname", ((TrackSegment) lt.getConnectD()).getId());
        }
        if (lv.isHidden()) {
            element.setAttribute("hidden", "yes");
        }
        if (!lt.getSignalAName().isEmpty()) {
            element.setAttribute("signalaname", lt.getSignalAName());
        }
        if (!lt.getSignalBName().isEmpty()) {
            element.setAttribute("signalbname", lt.getSignalBName());
        }
        if (!lt.getSignalCName().isEmpty()) {
            element.setAttribute("signalcname", lt.getSignalCName());
        }
        if (!lt.getSignalDName().isEmpty()) {
            element.setAttribute("signaldname", lt.getSignalDName());
        }
        Point2D coords = lv.getCoordsCenter();
        element.setAttribute("xcen", "" + coords.getX());
        element.setAttribute("ycen", "" + coords.getY());
        coords = lv.getCoordsA();
        element.setAttribute("xa", "" + coords.getX());
        element.setAttribute("ya", "" + coords.getY());
        coords = lv.getCoordsB();
        element.setAttribute("xb", "" + coords.getX());
        element.setAttribute("yb", "" + coords.getY());

        if (!lt.getSignalAMastName().isEmpty()) {
            element.addContent(new Element("signalAMast").addContent(lt.getSignalAMastName()));
        }

        if (!lt.getSignalBMastName().isEmpty()) {
            element.addContent(new Element("signalBMast").addContent(lt.getSignalBMastName()));
        }
        if (!lt.getSignalCMastName().isEmpty()) {
            element.addContent(new Element("signalCMast").addContent(lt.getSignalCMastName()));
        }
        if (!lt.getSignalDMastName().isEmpty()) {
            element.addContent(new Element("signalDMast").addContent(lt.getSignalDMastName()));
        }

        if (!lt.getSensorAName().isEmpty()) {
            element.addContent(new Element("sensorA").addContent(lt.getSensorAName()));
        }

        if (!lt.getSensorBName().isEmpty()) {
            element.addContent(new Element("sensorB").addContent(lt.getSensorBName()));
        }
        if (!lt.getSensorCName().isEmpty()) {
            element.addContent(new Element("sensorC").addContent(lt.getSensorCName()));
        }
        if (!lt.getSensorDName().isEmpty()) {
            element.addContent(new Element("sensorD").addContent(lt.getSensorDName()));
        }

        element.setAttribute("class", "jmri.jmrit.display.layoutEditor.configurexml.LevelXingXml"); // temporary // getClass().getName());
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
        LevelXing lt = new LevelXing(name, p);
        LevelXingView lv = new LevelXingView(lt, new Point2D.Double(x, y), p);

        // get remaining attributes
        Attribute a = element.getAttribute("blocknameac");
        if (a != null) {
            lt.tLayoutBlockNameAC = a.getValue();
        }
        a = element.getAttribute("blocknamebd");
        if (a != null) {
            lt.tLayoutBlockNameBD = a.getValue();
        }
        a = element.getAttribute("connectaname");
        if (a != null) {
            lt.connectAName = a.getValue();
        }
        a = element.getAttribute("connectbname");
        if (a != null) {
            lt.connectBName = a.getValue();
        }
        a = element.getAttribute("connectcname");
        if (a != null) {
            lt.connectCName = a.getValue();
        }
        a = element.getAttribute("connectdname");
        if (a != null) {
            lt.connectDName = a.getValue();
        }
        a = element.getAttribute("signalaname");
        if (a != null) {
            lt.setSignalAName(a.getValue());
        }
        a = element.getAttribute("signalbname");
        if (a != null) {
            lt.setSignalBName(a.getValue());
        }
        a = element.getAttribute("signalcname");
        if (a != null) {
            lt.setSignalCName(a.getValue());
        }
        a = element.getAttribute("signaldname");
        if (a != null) {
            lt.setSignalDName(a.getValue());
        }

        try {
            x = element.getAttribute("xa").getFloatValue();
            y = element.getAttribute("ya").getFloatValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert levelxing a coords attribute");
        }
        lv.setCoordsA(new Point2D.Double(x, y));

        try {
            x = element.getAttribute("xb").getFloatValue();
            y = element.getAttribute("yb").getFloatValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert levelxing b coords attribute");
        }
        lv.setCoordsB(new Point2D.Double(x, y));

        try {
            lv.setHidden(element.getAttribute("hidden").getBooleanValue());
        } catch (DataConversionException e1) {
            log.warn("unable to convert levelxing hidden attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
        }

        if (element.getChild("signalAMast") != null) {
            String mast = element.getChild("signalAMast").getText();
            if (mast != null && !mast.isEmpty()) {
                lt.setSignalAMast(mast);
            }
        }

        if (element.getChild("signalBMast") != null) {
            String mast = element.getChild("signalBMast").getText();
            if (mast != null && !mast.isEmpty()) {
                lt.setSignalBMast(mast);
            }
        }

        if (element.getChild("signalCMast") != null) {
            String mast = element.getChild("signalCMast").getText();
            if (mast != null && !mast.isEmpty()) {
                lt.setSignalCMast(mast);
            }
        }

        if (element.getChild("signalDMast") != null) {
            String mast = element.getChild("signalDMast").getText();
            if (mast != null && !mast.isEmpty()) {
                lt.setSignalDMast(mast);
            }
        }

        if (element.getChild("sensorA") != null) {
            String sensor = element.getChild("sensorA").getText();
            if (sensor != null && !sensor.isEmpty()) {
                lt.setSensorAName(sensor);
            }
        }

        if (element.getChild("sensorB") != null) {
            String sensor = element.getChild("sensorB").getText();
            if (sensor != null && !sensor.isEmpty()) {
                lt.setSensorBName(sensor);
            }
        }

        if (element.getChild("sensorC") != null) {
            String sensor = element.getChild("sensorC").getText();
            if (sensor != null && !sensor.isEmpty()) {
                lt.setSensorCName(sensor);
            }
        }

        if (element.getChild("sensorD") != null) {
            String sensor = element.getChild("sensorD").getText();
            if (sensor != null && !sensor.isEmpty()) {
                lt.setSensorDName(sensor);
            }
        }

        p.addLayoutTrack(lt, lv);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LevelXingViewXml.class);
}
