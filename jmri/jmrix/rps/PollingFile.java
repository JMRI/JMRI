package jmri.jmrix.rps;

import java.io.*;
import org.jdom.*;
import java.util.*;

import jmri.jmrit.XmlFile;
import javax.vecmath.Point3d;

/**
 * Persist RPS polling information
 * <P>
 * @author  Bob Jacobsen   Copyright 2008
 * @version $Revision: 1.1 $
 */
public class PollingFile extends XmlFile {

    Document doc;
    Element root;
    
    /**
     * Initialize for writing information.
     * <P>
     * This is followed by multiple "set" calls, then a "store"
     */
    public void prepare() {
        root = new Element("rpsfile");
        doc = newDocument(root, dtdLocation+"rpsroster.dtd");

        // add XSLT processing instruction
        // <?xml-stylesheet type="text/xsl" href="XSLT/rpsroster.xsl"?>
        java.util.Map m = new java.util.HashMap();
        m.put("type", "text/xsl");
        m.put("href", "http://jmri.sourceforge.net/xml/XSLT/rpsroster.xsl");
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0,p);
        
    }

    public void setPoll() {
        Element v = new Element("poll");
        v.setAttribute("active", Engine.instance().getPolling()?"true":"false");
        v.setAttribute("interval", ""+Engine.instance().getPollingInterval());
        root.addContent(v);
    }
    
    public void setTransmitter(int r) {
        Element e = new Element("transmitter");
        e.setAttribute("id", Engine.instance().getTransmitter(r).getID());
        e.setAttribute("address", ""+Engine.instance().getTransmitter(r).getAddress());
        e.setAttribute("long", Engine.instance().getTransmitter(r).isLongAddress()?"true":"false");
        e.setAttribute("poll", Engine.instance().getTransmitter(r).isPolled()?"true":"false");
        root.addContent(e);
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
    
    public boolean getPoll() {
        Element e = root.getChild("poll");
        Attribute a = e.getAttribute("active");
        if (a == null) return false;
        if (a.getValue().equals("true")) return true;
        return false;
    }
    
    /**
     * Get the transmitters from the file
     * @return null if not present
     */
    public List getTransmitters() {
        List kids = root.getChildren("receiver");
        for (int i = 0; i<kids.size(); i++) {
            Element e = (Element) kids.get(i);
            Attribute a = e.getAttribute("number");
            int num = -1;
            try { num = a.getIntValue(); }
            catch (org.jdom.DataConversionException ex) {}
        }
        return null;
    }
                
    static public String defaultLocation() {
        String location = XmlFile.prefsDir()+File.separator+"rps"+File.separator;
        XmlFile.ensurePrefsPresent(XmlFile.prefsDir());
        XmlFile.ensurePrefsPresent(location);
        return location;
    }
    static public String defaultFilename() {
        return defaultLocation()+"roster.xml";
    }
    
    // initialize logging
    static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PollingFile.class.getName());
}