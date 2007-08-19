// jmri.jmrit.display.configurexml.LayoutTurnoutXml.java

package jmri.jmrit.display.configurexml;

import jmri.configurexml.XmlAdapter;
import jmri.jmrit.display.LayoutEditor;
// import jmri.jmrit.display.PositionablePoint;
import jmri.jmrit.display.LayoutTurnout;
import jmri.jmrit.display.TrackSegment;
// import jmri.Sensor;
import jmri.Turnout;
import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Element;
import java.util.List;
import java.awt.Color;
import java.awt.geom.*;

/**
 * This module handles configuration for display.LayoutTurnout objects for a LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @version $Revision: 1.1 $
 */
public class LayoutTurnoutXml implements XmlAdapter {

    public LayoutTurnoutXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * LayoutTurnout
     * @param o Object to store, of type LayoutTurnout
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        LayoutTurnout p = (LayoutTurnout)o;

        Element element = new Element("layoutturnout");

        // include attributes
        element.setAttribute("ident", p.getName());
		if (p.getTurnoutName().length()>0) {
			element.setAttribute("turnoutname", p.getTurnoutName());
		}
		if (p.getBlockName().length()>0) {
			element.setAttribute("blockname", p.getBlockName());
		}
		if (p.getBlockBName().length()>0) {
			element.setAttribute("blockbname", p.getBlockBName());
		}
		if (p.getBlockCName().length()>0) {
			element.setAttribute("blockcname", p.getBlockCName());
		}
		if (p.getBlockDName().length()>0) {
			element.setAttribute("blockdname", p.getBlockDName());
		}
		element.setAttribute("type", ""+p.getTurnoutType());
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
		element.setAttribute("continuing", ""+p.getContinuingSense());		
		Point2D coords = p.getCoordsCenter();
		element.setAttribute("xcen", ""+coords.getX());
		element.setAttribute("ycen", ""+coords.getY());
		coords = p.getCoordsB();
		element.setAttribute("xb", ""+coords.getX());
		element.setAttribute("yb", ""+coords.getY());		
		coords = p.getCoordsC();
		element.setAttribute("xc", ""+coords.getX());
		element.setAttribute("yc", ""+coords.getY());

        element.setAttribute("class", "jmri.jmrit.display.configurexml.LayoutTurnoutXml");
        return element;
    }

    public void load(Element element) {
        log.error("Invalid method called");
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
		int tType = LayoutTurnout.RH_TURNOUT;
		try {
			x = element.getAttribute("xcen").getFloatValue();
			y = element.getAttribute("ycen").getFloatValue();
			tType = element.getAttribute("type").getIntValue();
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert layoutturnout attribute");
        }
		
		// create the new LayoutTurnout
        LayoutTurnout l = new LayoutTurnout(name,tType,
							new Point2D.Double(x,y),0.0,1.0,1.0,p);
		
		// get remaining attributes
		Attribute a = element.getAttribute("blockname");
		if (a != null) {
			l.tBlockName = a.getValue();
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
		a = element.getAttribute("turnoutname");
		if (a != null) {
			l.tTurnoutName = a.getValue();
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
		a = element.getAttribute("continuing");
		if (a != null) {
			int continuing = Turnout.CLOSED;
			try {
				continuing = element.getAttribute("continuing").getIntValue();
			} catch (org.jdom.DataConversionException e) {
				log.error("failed to convert continuingsense attribute");
			}
			l.setContinuingSense(continuing);
		}
		try {
			x = element.getAttribute("xb").getFloatValue();
			y = element.getAttribute("yb").getFloatValue();
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert layoutturnout b coords attribute");
        }
		l.setCoordsB(new Point2D.Double(x,y));
		try {
			x = element.getAttribute("xc").getFloatValue();
			y = element.getAttribute("yc").getFloatValue();
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert layoutturnout c coords attribute");
        }
		l.setCoordsC(new Point2D.Double(x,y));

		p.turnoutList.add(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LayoutTurnoutXml.class.getName());
}