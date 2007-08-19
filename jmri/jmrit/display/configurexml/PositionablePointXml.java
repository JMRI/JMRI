// jmri.jmrit.display.configurexml.PositionablePointXml.java

package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.display.LayoutEditor;
import jmri.jmrit.display.PositionablePoint;
import jmri.jmrit.display.TrackSegment;
import jmri.Sensor;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import java.util.List;
import java.awt.Color;
import java.awt.geom.*;

/**
 * This module handles configuration for display.PositionablePoint objects for a LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @version $Revision: 1.1 $
 */
public class PositionablePointXml implements XmlAdapter {

    public PositionablePointXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * PositionablePoint
     * @param o Object to store, of type PositionablePoint
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        PositionablePoint p = (PositionablePoint)o;

        Element element = new Element("positionablepoint");

        // include attributes
        element.setAttribute("ident", p.getID());
        element.setAttribute("type", ""+p.getType());
		Point2D coords = p.getCoords();
		element.setAttribute("x", ""+coords.getX());
		element.setAttribute("y", ""+coords.getY());
		if (p.getConnect1() != null) {
			element.setAttribute("connect1name", ((TrackSegment)p.getConnect1()).getID());
		}
		if (p.getConnect2() != null) {
			element.setAttribute("connect2name", ((TrackSegment)p.getConnect2()).getID());
		}

        element.setAttribute("class", "jmri.jmrit.display.configurexml.PositionablePointXml");
        return element;
    }

    public void load(Element element) {
        log.error("Invalid method called");
    }

    /**
     * Load, starting with the layoutblock element, then
     * all the value-icon pairs
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor)o;
		
		// get attributes
        String name = element.getAttribute("ident").getValue();
		Point2D coords;
		int type = PositionablePoint.ANCHOR;
		double x = 0.0;
		double y = 0.0;
		try {
			x = element.getAttribute("x").getFloatValue();
			y = element.getAttribute("y").getFloatValue();
			type = element.getAttribute("type").getIntValue();
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert positionablepoint attribute");
        }
		
		// create the new PositionablePoint
        PositionablePoint l = new PositionablePoint(name,type,new Point2D.Double(x,y),p);
		
		// get remaining attributes
		Attribute a = element.getAttribute("connect1name");
		if (a != null) {
			l.trackSegment1Name = a.getValue();
		}
		a = element.getAttribute("connect2name");
		if (a != null) {
			l.trackSegment2Name = a.getValue();
		}
		p.pointList.add(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PositionablePointXml.class.getName());
}