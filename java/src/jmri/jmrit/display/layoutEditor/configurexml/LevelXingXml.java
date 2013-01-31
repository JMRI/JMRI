// jmri.jmrit.display.configurexml.LevelXingXml.java

package jmri.jmrit.display.layoutEditor.configurexml;

import jmri.configurexml.AbstractXmlAdapter;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import org.jdom.Attribute;
import org.jdom.Element;
import java.awt.geom.*;

/**
 * This module handles configuration for display.LevelXing objects for a LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @version $Revision$
 */
public class LevelXingXml extends AbstractXmlAdapter {

    public LevelXingXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LevelXing
     * @param o Object to store, of type LevelXing
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        LevelXing p = (LevelXing)o;

        Element element = new Element("levelxing");

        // include attributes
        element.setAttribute("ident", p.getID());
		if (p.getBlockNameAC().length()>0) {
			element.setAttribute("blocknameac", p.getBlockNameAC());
		}
		if (p.getBlockNameBD().length()>0) {
			element.setAttribute("blocknamebd", p.getBlockNameBD());
		}
		if (p.getConnectA()!=null) {
			element.setAttribute("connectaname", ((TrackSegment)p.getConnectA()).getID());
		}
		if (p.getConnectB()!=null) {
			element.setAttribute("connectbname", ((TrackSegment)p.getConnectB()).getID());
		}
		if (p.getConnectC()!=null) {
			element.setAttribute("connectcname", ((TrackSegment)p.getConnectC()).getID());
		}
		if (p.getConnectD()!=null) {
			element.setAttribute("connectdname", ((TrackSegment)p.getConnectD()).getID());
		}
		if (p.getSignalAName().length()>0) {
			element.setAttribute("signalaname", p.getSignalAName());
		}
		if (p.getSignalBName().length()>0) {
			element.setAttribute("signalbname", p.getSignalBName());
		}
		if (p.getSignalCName().length()>0) {
			element.setAttribute("signalcname", p.getSignalCName());
		}
		if (p.getSignalDName().length()>0) {
			element.setAttribute("signaldname", p.getSignalDName());
		}
		Point2D coords = p.getCoordsCenter();
		element.setAttribute("xcen", ""+coords.getX());
		element.setAttribute("ycen", ""+coords.getY());
		coords = p.getCoordsA();
		element.setAttribute("xa", ""+coords.getX());
		element.setAttribute("ya", ""+coords.getY());
		coords = p.getCoordsB();
		element.setAttribute("xb", ""+coords.getX());
		element.setAttribute("yb", ""+coords.getY());
        
        if(p.getSignalAMastName().length()>0){
            element.addContent(new Element("signalAMast").addContent(p.getSignalAMastName()));
        }
        
        if(p.getSignalBMastName().length()>0){
            element.addContent(new Element("signalBMast").addContent(p.getSignalBMastName()));
        }
        if(p.getSignalCMastName().length()>0){
            element.addContent(new Element("signalCMast").addContent(p.getSignalCMastName()));
        }
        if(p.getSignalDMastName().length()>0){
            element.addContent(new Element("signalDMast").addContent(p.getSignalDMastName()));
        }
        
        if(p.getSensorAName().length()>0){
            element.addContent(new Element("sensorA").addContent(p.getSensorAName()));
        }
        
        if(p.getSensorBName().length()>0){
            element.addContent(new Element("sensorB").addContent(p.getSensorBName()));
        }
        if(p.getSensorCName().length()>0){
            element.addContent(new Element("sensorC").addContent(p.getSensorCName()));
        }
        if(p.getSensorDName().length()>0){
            element.addContent(new Element("sensorD").addContent(p.getSensorDName()));
        }
        
        element.setAttribute("class", "jmri.jmrit.display.layoutEditor.configurexml.LevelXingXml");
        return element;
    }

    public boolean load(Element element) {
        log.error("Invalid method called");
        return false;
    }

    /**
     * Load, starting with the levelxing element, then
     * all the other data
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor)o;
		
		// get center point
        String name = element.getAttribute("ident").getValue();
		double x = 0.0;
		double y = 0.0;
		try {
			x = element.getAttribute("xcen").getFloatValue();
			y = element.getAttribute("ycen").getFloatValue();
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert levelxing center  attribute");
        }
		
		// create the new LevelXing
        LevelXing l = new LevelXing(name,new Point2D.Double(x,y),p);
		
		// get remaining attributes
		Attribute a = element.getAttribute("blocknameac");
		if (a != null) {
			l.tBlockNameAC = a.getValue();
		}
		a = element.getAttribute("blocknamebd");
		if (a != null) {
			l.tBlockNameBD = a.getValue();
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
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert levelxing a coords attribute");
        }
		l.setCoordsA(new Point2D.Double(x,y));
		try {
			x = element.getAttribute("xb").getFloatValue();
			y = element.getAttribute("yb").getFloatValue();
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert levelxing b coords attribute");
        }
		l.setCoordsB(new Point2D.Double(x,y));
        
        if (element.getChild("signalAMast")!=null){
            String mast = element.getChild("signalAMast").getText();
            if (mast!=null && !mast.equals("")){
                l.setSignalAMast(mast);
            }
        }
        
        if (element.getChild("signalBMast")!=null){
            String mast = element.getChild("signalBMast").getText();
            if (mast!=null && !mast.equals("")){
                l.setSignalBMast(mast);
            }
        }
        
        if (element.getChild("signalCMast")!=null){
            String mast = element.getChild("signalCMast").getText();
            if (mast!=null && !mast.equals("")){
                l.setSignalCMast(mast);
            }
        }

        if (element.getChild("signalDMast")!=null){
            String mast = element.getChild("signalDMast").getText();
            if (mast!=null && !mast.equals("")){
                l.setSignalDMast(mast);
            }
        }
        
        if (element.getChild("sensorA")!=null){
            String sensor = element.getChild("sensorA").getText();
            if (sensor!=null && !sensor.equals("")){
                l.setSensorAName(sensor);
            }
        }
        
        if (element.getChild("sensorB")!=null){
            String sensor = element.getChild("sensorB").getText();
            if (sensor!=null && !sensor.equals("")){
                l.setSensorBName(sensor);
            }
        }
        
        if (element.getChild("sensorC")!=null){
            String sensor = element.getChild("sensorC").getText();
            if (sensor!=null && !sensor.equals("")){
                l.setSensorCName(sensor);
            }
        }

        if (element.getChild("sensorD")!=null){
            String sensor = element.getChild("sensorD").getText();
            if (sensor!=null && !sensor.equals("")){
                l.setSensorDName(sensor);
            }
        }


		p.xingList.add(l);
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LevelXingXml.class.getName());
}