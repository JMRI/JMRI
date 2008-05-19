package jmri.jmrix.rps;

import java.io.*;
import org.jdom.*;
import java.util.*;

import jmri.jmrit.XmlFile;
import javax.vecmath.Point3d;

/**
 * Persist RPS configuration information
 * <P>
 * @author  Bob Jacobsen   Copyright 2007
 * @version $Revision: 1.4 $
 */
public class PositionFile extends XmlFile {

    Document doc;
    Element root;
    
    /**
     * Initialize for writing information.
     * <P>
     * This is followed by multiple "set" calls, then a "store"
     */
    public void prepare() {
        root = new Element("rpsfile");
        doc = newDocument(root, dtdLocation+"rpsfile.dtd");

        // add XSLT processing instruction
        // <?xml-stylesheet type="text/xsl" href="XSLT/rpsfile.xsl"?>
        java.util.Map m = new java.util.HashMap();
        m.put("type", "text/xsl");
        m.put("href", "http://jmri.sourceforge.net/xml/XSLT/rpsfile.xsl");
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0,p);
        
    }

    public void setConstants(double vSound, int offset) {
        Element v = new Element("vsound");
        v.addContent(""+vSound);
        root.addContent(v);
        
        Element o = new Element("offset");
        o.addContent(""+offset);
        root.addContent(o);
    }
    
    public void setReceiver(int n, Receiver r) {
        Element e = new Element("receiver");
        e.setAttribute("number", ""+n);
        e.addContent(positionElement(r.getPosition()));
        root.addContent(e);
    }

    public void setReceiver(int n, Point3d p) {
        Element e = new Element("receiver");
        e.setAttribute("number", ""+n);
        e.addContent(positionElement(p));
        root.addContent(e);
    }

    public void setCalibrationPoint(Point3d p, Reading r) {
        Element e = new Element("calibrationpoint");
        e.addContent(positionElement(p));
        e.addContent(readingElement(r));
        root.addContent(e);
    }

    Element positionElement(Point3d p) {
        Element e = new Element("position");
        Element x = new Element("x");
        x.addContent(""+p.x);
        e.addContent(x);
        Element y = new Element("y");
        y.addContent(""+p.y);
        e.addContent(y);
        Element z = new Element("z");
        z.addContent(""+p.z);
        e.addContent(z);
        return e;
    }
    
    public Point3d positionFromElement(Element position) {
        Element e;
        e = position.getChild("x");
        float x = Float.parseFloat(e.getText());
        
        e = position.getChild("y");
        float y = Float.parseFloat(e.getText());
        
        e = position.getChild("z");
        float z = Float.parseFloat(e.getText());
        
        return new Point3d(x, y, z);
    }

    Element readingElement(Reading r) {
        Element e = new Element("reading");
        Element c = new Element("id");
        c.addContent(""+r.getID());
        e.addContent(c);
        for (int i = 0; i<r.getNSample(); i++) {
            e.addContent(timeElement(r.getValue(i)));
        }
        return e;
    }
    
    public Reading readingFromElement(Element reading) {
        int id = Integer.parseInt(reading.getChild("id").getText());
        List kids = reading.getChildren("time");
        int count = kids.size();
        double[] vals = new double[count];
        
        for (int i = 0; i<count; i++) {
            Element e = (Element)kids.get(i);
            double val = Double.parseDouble(e.getText());
            vals[i] = val;
        }
        
        return new Reading(id, vals);
    }
    
    Element timeElement(double time) {
        Element e = new Element("time");
        e.addContent(""+time);
        return e;
    }
    
    public void store(File file) throws JDOMException, IOException {
        writeXML(file, doc);
    }

    /**
     * Read in the file, and make available
     * for examination
     */
    public void loadFile(File f) 
            throws org.jdom.JDOMException, java.io.IOException {
            
        root = rootFromFile(f);
    }
    
    public double getVSound() {
        Element e = root.getChild("vsound");
        return Double.parseDouble(e.getText());
    }
    
    public int getOffset() {
        Element e = root.getChild("offset");
        return Integer.parseInt(e.getText());
    }
    
    /**
     * FInd the highest numbered receiver in the file
     */
    public int maxReceiver() {
        List kids = root.getChildren("receiver");
        int max = -1;
        for (int i = 0; i<kids.size(); i++) {
            Attribute a = ((Element) kids.get(i)).getAttribute("number");
            if (a==null) continue;
            int n = -1;
            try { n = a.getIntValue(); }
            catch (org.jdom.DataConversionException e) {}
            max = Math.max(max, n);
        }
        return max;
    }
    
    /**
     * Get the nth receiver position in the file.
     * @return null if not present
     */
    public Point3d getReceiverPosition(int n) {
        List kids = root.getChildren("receiver");
        for (int i = 0; i<kids.size(); i++) {
            Element e = (Element) kids.get(i);
            Attribute a = e.getAttribute("number");
            if (a == null) continue;
            int num = -1;
            try { num = a.getIntValue(); }
            catch (org.jdom.DataConversionException ex) {}
            if (num == n) return positionFromElement(e.getChild("position"));
        }
        return null;
    }
        
    /**
     * Get the nth calibration position in the file.
     * @return null if not present
     */
    public Point3d getCalibrationPosition(int n) {
        List kids = root.getChildren("calibrationpoint");
        if (n>= kids.size()) return null;
        Element e = (Element) kids.get(n);
        return positionFromElement(e.getChild("position"));
    }
        
    /**
     * Get the nth calibration reading in the file.
     * @return null if not present
     */
    public Reading getCalibrationReading(int n) {
        List kids = root.getChildren("calibrationpoint");
        if (n>= kids.size()) return null;
        Element e = (Element) kids.get(n);
        return readingFromElement(e.getChild("reading"));
    }
        
    static public String defaultLocation() {
        String location = XmlFile.prefsDir()+File.separator+"rps"+File.separator;
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        XmlFile.ensurePrefsPresent(location);
        return location;
    }
    static public String defaultFilename() {
        return defaultLocation()+"positions.xml";
    }
    
    // initialize logging
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PositionFile.class.getName());
}