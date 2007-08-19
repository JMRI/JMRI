// jmri.jmrit.display.configurexml.TrackSegmentXml.java

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
 * This module handles configuration for display.TrackSegment objects for a LayoutEditor.
 *
 * @author David Duchamp Copyright (c) 2007
 * @version $Revision: 1.1 $
 */
public class TrackSegmentXml implements XmlAdapter {

    public TrackSegmentXml() {
    }

    /**
     * Default implementation for storing the contents of a
     * TrackSegment
     * @param o Object to store, of type TrackSegment
     * @return Element containing the complete info
     */
    public Element store(Object o) {

        TrackSegment p = (TrackSegment)o;

        Element element = new Element("tracksegment");

        // include attributes
        element.setAttribute("ident", p.getID());
		if (p.getBlockName().length()>0) {
			element.setAttribute("blockname", p.getBlockName());
		}
		element.setAttribute("connect1name", p.getConnect1Name());
		element.setAttribute("type1", ""+p.getType1());
		element.setAttribute("connect2name", p.getConnect2Name());
		element.setAttribute("type2", ""+p.getType2());
        element.setAttribute("dashed", ""+(p.getDashed()?"yes":"no"));
        element.setAttribute("mainline", ""+(p.getMainline()?"yes":"no"));
        element.setAttribute("hidden", ""+(p.getHidden()?"yes":"no"));

        element.setAttribute("class", "jmri.jmrit.display.configurexml.TrackSegmentXml");
        return element;
    }

    public void load(Element element) {
        log.error("Invalid method called");
    }

    /**
     * Load, starting with the tracksegment element, then
     * all all attributes
     * @param element Top level Element to unpack.
     * @param o  LayoutEditor as an Object
     */
    public void load(Element element, Object o) {
        // create the objects
        LayoutEditor p = (LayoutEditor)o;
		
		// get attributes
        String name = element.getAttribute("ident").getValue();
		int type1 = LayoutEditor.NONE;
		int type2 = LayoutEditor.NONE;
		try {
			type1 = element.getAttribute("type1").getIntValue();
			type2 = element.getAttribute("type2").getIntValue();
		} catch (org.jdom.DataConversionException e) {
            log.error("failed to convert tracksegment attribute");
        }
        boolean dash = true;
        if (element.getAttribute("dashed").getValue().equals("no"))
            dash = false;
        boolean main = true;
        if (element.getAttribute("mainline").getValue().equals("no"))
            main = false;
        boolean hide = true;
        if (element.getAttribute("hidden").getValue().equals("no"))
            hide = false;
		String con1Name = element.getAttribute("connect1name").getValue();
		String con2Name = element.getAttribute("connect2name").getValue();
		
		// create the new TrackSegment
        TrackSegment l = new TrackSegment(name,con1Name,type1,con2Name,type2,
									dash,main,hide,p);
		
		// get remaining attribute
		Attribute a = element.getAttribute("blockname");
		if (a != null) {
			l.tBlockName = a.getValue();
		}
		p.trackList.add(l);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(TrackSegmentXml.class.getName());
}