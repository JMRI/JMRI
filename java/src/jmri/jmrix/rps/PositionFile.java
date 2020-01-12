package jmri.jmrix.rps;

import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.vecmath.Point3d;
import jmri.jmrit.XmlFile;
import jmri.util.FileUtil;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.ProcessingInstruction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Persist RPS configuration information.
 *
 * @author Bob Jacobsen Copyright 2007, 2008
 */
public class PositionFile extends XmlFile {

    Document doc;
    Element root;

    /**
     * Initialize for writing information.
     * <p>
     * This is followed by multiple "set" calls, then a "store".
     */
    public void prepare() {
        root = new Element("rpsfile");
        doc = newDocument(root, dtdLocation + "rpsfile.dtd");

        // add XSLT processing instruction
        // <?xml-stylesheet type="text/xsl" href="XSLT/rpsfile.xsl"?>
        java.util.Map<String, String> m = new java.util.HashMap<String, String>();
        m.put("type", "text/xsl");
        m.put("href", xsltLocation + "rpsfile.xsl");
        ProcessingInstruction p = new ProcessingInstruction("xml-stylesheet", m);
        doc.addContent(0, p);

    }

    public void setConstants(double vSound, int offset, String algorithm) {
        Element v = new Element("vsound");
        v.addContent("" + vSound);
        root.addContent(v);

        Element o = new Element("offset");
        o.addContent("" + offset);
        root.addContent(o);

        Element a = new Element("algorithm");
        a.addContent(algorithm);
        root.addContent(a);
    }

    public void setReceiver(int n, Receiver r) {
        Element e = new Element("receiver");
        e.setAttribute("number", "" + n);
        e.setAttribute("active", "" + (r.isActive() ? "true" : "false"));
        e.setAttribute("mintime", "" + r.getMinTime());
        e.setAttribute("maxtime", "" + r.getMaxTime());
        e.addContent(positionElement(r.getPosition()));
        root.addContent(e);
    }

    public void setReceiver(int n, Point3d p, boolean active) {
        // allows defaults for min, max time
        Element e = new Element("receiver");
        e.setAttribute("number", "" + n);
        e.setAttribute("active", "" + (active ? "true" : "false"));
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
        x.addContent("" + p.x);
        e.addContent(x);
        Element y = new Element("y");
        y.addContent("" + p.y);
        e.addContent(y);
        Element z = new Element("z");
        z.addContent("" + p.z);
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
        c.addContent("" + r.getId());
        e.addContent(c);
        for (int i = 1; i <= r.getNValues(); i++) {
            e.addContent(timeElement(r.getValue(i)));
        }
        return e;
    }

    public Reading readingFromElement(Element reading) {
        String id = reading.getChild("id").getText();
        List<Element> kids = reading.getChildren("time");
        int count = kids.size();
        double[] vals = new double[count + 1];

        for (int i = 0; i < count; i++) {
            Element e = kids.get(i);
            double val = Double.parseDouble(e.getText());
            vals[i + 1] = val;  // 1st item goes in element 1
        }

        return new Reading(id, vals);
    }

    Element timeElement(double time) {
        Element e = new Element("time");
        e.addContent("" + time);
        return e;
    }

    public void store(File file) throws IOException {
        writeXML(file, doc);
    }

    /**
     * Read in the file, and make available for examination
     */
    public void loadFile(File f)
            throws org.jdom2.JDOMException, java.io.IOException {

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

    public String getAlgorithm() {
        Element e = root.getChild("algorithm");
        return e.getText();
    }

    /**
     * FInd the highest numbered receiver in the file
     */
    public int maxReceiver() {
        List<Element> kids = root.getChildren("receiver");
        int max = -1;
        for (int i = 0; i < kids.size(); i++) {
            Attribute a = kids.get(i).getAttribute("number");
            if (a == null) {
                continue;
            }
            int n = -1;
            try {
                n = a.getIntValue();
            } catch (org.jdom2.DataConversionException e) {
                log.error("in maxReceiver", e);
            }
            max = Math.max(max, n);
        }
        return max;
    }

    /**
     * Get the nth receiver position in the file.
     *
     * @return null if not present
     */
    public Point3d getReceiverPosition(int n) {
        List<Element> kids = root.getChildren("receiver");
        for (int i = 0; i < kids.size(); i++) {
            Element e = kids.get(i);
            Attribute a = e.getAttribute("number");
            if (a == null) {
                continue;
            }
            int num = -1;
            try {
                num = a.getIntValue();
            } catch (org.jdom2.DataConversionException ex) {
                log.error("in getReceiverPosition", ex);
            }
            if (num == n) {
                return positionFromElement(e.getChild("position"));
            }
        }
        return null;
    }

    /**
     * Get the nth receiver active state in the file.
     *
     * @return true if not present
     */
    public boolean getReceiverActive(int n) {
        List<Element> kids = root.getChildren("receiver");
        for (int i = 0; i < kids.size(); i++) {
            Element e = kids.get(i);
            Attribute a = e.getAttribute("number");
            if (a == null) {
                continue;
            }
            int num = -1;
            try {
                num = a.getIntValue();
            } catch (org.jdom2.DataConversionException ex) {
                log.error("in getReceiverActive", ex);
            }
            if (num != n) {
                continue;
            }
            a = e.getAttribute("active");
            if (a == null) {
                return true; // default value
            }
            if (a.getValue().equals("false")) {
                return false;
            }
            return true;
        }
        return true;
    }

    /**
     * Get the nth receiver min time.
     *
     * @return 0 if not present
     */
    public int getReceiverMin(int n) {
        List<Element> kids = root.getChildren("receiver");
        for (int i = 0; i < kids.size(); i++) {
            Element e = kids.get(i);
            Attribute a = e.getAttribute("number");
            if (a == null) {
                continue;
            }
            int num = -1;
            try {
                num = a.getIntValue();
            } catch (org.jdom2.DataConversionException ex1) {
                log.error("in getReceiverMin", ex1);
            }
            if (num != n) {
                continue;
            }
            a = e.getAttribute("mintime");
            if (a == null) {
                return 0; // default value
            }
            try {
                return a.getIntValue();
            } catch (org.jdom2.DataConversionException ex2) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * Get the nth receiver max time.
     *
     * @return 0 if not present
     */
    public int getReceiverMax(int n) {
        List<Element> kids = root.getChildren("receiver");
        for (int i = 0; i < kids.size(); i++) {
            Element e = kids.get(i);
            Attribute a = e.getAttribute("number");
            if (a == null) {
                continue;
            }
            int num = -1;
            try {
                num = a.getIntValue();
            } catch (org.jdom2.DataConversionException ex1) {
                log.error("in getReceiverMax", ex1);
            }
            if (num != n) {
                continue;
            }
            a = e.getAttribute("maxtime");
            if (a == null) {
                return 99999; // default value
            }
            try {
                return a.getIntValue();
            } catch (org.jdom2.DataConversionException ex2) {
                return 99999;
            }
        }
        return 99999;
    }

    /**
     * Get the nth calibration position in the file.
     *
     * @return null if not present
     */
    public Point3d getCalibrationPosition(int n) {
        List<Element> kids = root.getChildren("calibrationpoint");
        if (n >= kids.size()) {
            return null;
        }
        Element e = kids.get(n);
        return positionFromElement(e.getChild("position"));
    }

    /**
     * Get the nth calibration reading in the file.
     *
     * @return null if not present
     */
    public Reading getCalibrationReading(int n) {
        List<Element> kids = root.getChildren("calibrationpoint");
        if (n >= kids.size()) {
            return null;
        }
        Element e = kids.get(n);
        return readingFromElement(e.getChild("reading"));
    }

    static public String defaultLocation() {
        String location = FileUtil.getUserFilesPath() + "rps" + File.separator;
        FileUtil.createDirectory(location);
        return location;
    }

    static public String defaultFilename() {
        return defaultLocation() + "positions.xml";
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(PositionFile.class);

}
