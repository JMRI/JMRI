// DecoderFileTest.java

package jmri.jmrit.decoderdefn;

import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;
import org.jdom.*;
import jmri.jmrit.symbolicprog.*;
import jmri.progdebugger.*;

/**
 * DecoderFileTest.java
 *
 * @author			Bob Jacobsen, Copyright (C) 2001, 2002
 * @version         $Revision$
 */
public class DecoderFileTest extends TestCase {

    ProgDebugger p = new ProgDebugger();

    public void testSingleVersionNumber() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
                                        "family", "filename", 16, 3, null);
        d.setOneVersion(18);
        Assert.assertEquals("single 18 OK", true, d.isVersion(18));
        Assert.assertEquals("single 19 not OK", false, d.isVersion(19));
    }

    public void testRangeVersionNumber() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "24", "25",
                                        "family", "filename", 16, 3, null);
        d.setVersionRange(18, 22);
        Assert.assertEquals("single 17 not OK", false, d.isVersion(17));
        Assert.assertEquals("single 18 OK", true, d.isVersion(18));
        Assert.assertEquals("single 19 OK", true, d.isVersion(19));
        Assert.assertEquals("single 20 OK", true, d.isVersion(20));
        Assert.assertEquals("single 21 OK", true, d.isVersion(21));
        Assert.assertEquals("single 22 OK", true, d.isVersion(22));
        Assert.assertEquals("single 23 not OK", false, d.isVersion(23));
    }

    public void testCtorRange() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "18", "22",
                                        "family", "filename", 16, 3, null);
        Assert.assertEquals("single 17 not OK", false, d.isVersion(17));
        Assert.assertEquals("single 18 OK", true, d.isVersion(18));
        Assert.assertEquals("single 19 OK", true, d.isVersion(19));
        Assert.assertEquals("single 20 OK", true, d.isVersion(20));
        Assert.assertEquals("single 21 OK", true, d.isVersion(21));
        Assert.assertEquals("single 22 OK", true, d.isVersion(22));
        Assert.assertEquals("single 23 not OK", false, d.isVersion(23));
    }

    public void testCtorLow() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "18", null,
                                        "family", "filename", 16, 3, null);
        Assert.assertEquals("single 17 not OK", false, d.isVersion(17));
        Assert.assertEquals("single 18 OK", true, d.isVersion(18));
        Assert.assertEquals("single 19 not OK", false, d.isVersion(19));
    }

    public void testCtorHigh() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", null, "18",
                                        "family", "filename", 16, 3, null);
        Assert.assertEquals("single 17 not OK", false, d.isVersion(17));
        Assert.assertEquals("single 18 OK", true, d.isVersion(18));
        Assert.assertEquals("single 19 not OK", false, d.isVersion(19));
    }

    public void testSeveralSingleVersionNumber() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
                                        "family", "filename", 16, 3, null);
        d.setOneVersion(18);
        Assert.assertEquals("single 18 OK", true, d.isVersion(18));
        Assert.assertEquals("single 19 not OK", false, d.isVersion(19));
        d.setOneVersion(19);
        Assert.assertEquals("single 19 OK", true, d.isVersion(19));
        Assert.assertEquals("single 21 not OK", false, d.isVersion(21));
        d.setOneVersion(21);
        Assert.assertEquals("single 21 OK", true, d.isVersion(21));
    }

    public void testMfgName() {
        setupDecoder();
        Assert.assertEquals("mfg name ", "Digitrax", DecoderFile.getMfgName(decoder));
    }

    public void testLoadTable() {
        setupDecoder();

        // this test should probably be done in terms of a test class instead of the real one...
        JLabel progStatus            = new JLabel(" OK ");
        CvTableModel cvModel         = new CvTableModel(progStatus, p);
        IndexedCvTableModel icvModel = new IndexedCvTableModel(new JLabel(), p);
        VariableTableModel variableModel = new VariableTableModel(progStatus,
                                                                  new String[]  {"Name", "Value"},
                                                                  cvModel, icvModel);
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
                                        "family", "filename", 16, 16, null);

        d.loadVariableModel(decoder, variableModel);
        Assert.assertEquals("read rows ", 3, variableModel.getRowCount());
        Assert.assertEquals("first row name ", "Address", variableModel.getLabel(0));
        Assert.assertEquals("third row name ", "Normal direction of motion", variableModel.getLabel(2));
    }

    public void testIncludeCheck() {
        Element e;
        // test some examples
        e = new Element("Test");
        Assert.assertTrue("1 in null",DecoderFile.isIncluded(e, "1,2"));

        (e = new Element("Test")). setAttribute("include", "1,2");
        Assert.assertTrue("1 in 1,2",DecoderFile.isIncluded(e, "1"));
        Assert.assertTrue("2 in 1,2",DecoderFile.isIncluded(e, "2"));
        Assert.assertTrue("3 in 1,2",!DecoderFile.isIncluded(e, "3"));
        
        (e = new Element("Test")). setAttribute("include", "105,205");
        Assert.assertTrue("105 in 105,205",DecoderFile.isIncluded(e, "105"));

        (e = new Element("Test")). setAttribute("include", "205,105");
        Assert.assertTrue("105 in 205,105",DecoderFile.isIncluded(e, "105"));

        (e = new Element("Test")). setAttribute("include", "1050,205");
        Assert.assertTrue("105 not in 1050,205",!DecoderFile.isIncluded(e, "105"));

        (e = new Element("Test")). setAttribute("include", "50,1050");
        Assert.assertTrue("105 not in 50,1050",!DecoderFile.isIncluded(e, "105"));

        (e = new Element("Test")). setAttribute("include", "827004,827008,827104,827108,827106,828043,828045,828047");
        Assert.assertTrue("827004", DecoderFile.isIncluded(e, "827004"));
        Assert.assertTrue("Not 827005", !DecoderFile.isIncluded(e, "827005"));
        Assert.assertTrue("827108", DecoderFile.isIncluded(e, "827108"));
    }
    
    public void testExcludeCheck() {
        Element e;
        // test some examples
        e = new Element("Test");
        Assert.assertTrue("1 in null",DecoderFile.isIncluded(e, "1,2"));

        (e = new Element("Test")). setAttribute("exclude", "1,2");
        Assert.assertTrue("1 in 1,2",!DecoderFile.isIncluded(e, "1"));
        Assert.assertTrue("2 in 1,2",!DecoderFile.isIncluded(e, "2"));
        Assert.assertTrue("3 in 1,2",DecoderFile.isIncluded(e, "3"));
        
        (e = new Element("Test")). setAttribute("exclude", "105,205");
        Assert.assertTrue("105 in 105,205",!DecoderFile.isIncluded(e, "105"));

        (e = new Element("Test")). setAttribute("exclude", "205,105");
        Assert.assertTrue("105 in 205,105",!DecoderFile.isIncluded(e, "105"));

        (e = new Element("Test")). setAttribute("exclude", "1050,205");
        Assert.assertTrue("105 not in 1050,205",DecoderFile.isIncluded(e, "105"));

        (e = new Element("Test")). setAttribute("exclude", "50,1050");
        Assert.assertTrue("105 not in 50,1050",DecoderFile.isIncluded(e, "105"));

        (e = new Element("Test")). setAttribute("exclude", "827004,827008,827104,827108,827106,828043,828045,828047");
        Assert.assertTrue("827004", !DecoderFile.isIncluded(e, "827004"));
        Assert.assertTrue("Not 827005", DecoderFile.isIncluded(e, "827005"));
        Assert.assertTrue("827108", !DecoderFile.isIncluded(e, "827108"));
    }
    
    public void testMinOut() {
        setupDecoder();

        // this test should probably be done in terms of a test class instead of the real one...
        JLabel progStatus            = new JLabel(" OK ");
        CvTableModel cvModel         = new CvTableModel(progStatus, p);
        IndexedCvTableModel icvModel = new IndexedCvTableModel(new JLabel(), p);
        VariableTableModel variableModel = new VariableTableModel(progStatus,
                                                                  new String[]  {"Name", "Value"},
                                                                  cvModel, icvModel);
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
                                        "family", "filename", 16, 3, null);

        d.loadVariableModel(decoder, variableModel);
        Assert.assertEquals("read rows ", 2, variableModel.getRowCount());
    }

    public void testMinFn() {
        setupDecoder();

        // this test should probably be done in terms of a test class instead of the real one...
        JLabel progStatus            = new JLabel(" OK ");
        CvTableModel cvModel         = new CvTableModel(progStatus, p);
        IndexedCvTableModel icvModel = new IndexedCvTableModel(new JLabel(), p);
        VariableTableModel variableModel = new VariableTableModel(progStatus,
                                                                  new String[]  {"Name", "Value"},
                                                                  cvModel, icvModel);
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
                                        "family", "filename", 3, 16, null);

        d.loadVariableModel(decoder, variableModel);
        Assert.assertEquals("read rows ", 2, variableModel.getRowCount());
    }

    // static variables for the test XML structures
    Element root = null;
    public Element decoder = null;
    Document doc = null;

    // provide a test document in the above static variables
    public void setupDecoder() {
        // create a JDOM tree with just some elements
        root = new Element("decoder-config");
        doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));

        // add some elements
        root.addContent(decoder = new Element("decoder")
            .addContent(new Element("family")
                .setAttribute("family","DH142 etc")
                .setAttribute("mfg","Digitrax")
                .setAttribute("defnVersion","242")
                .setAttribute("comment","DH142 decoder: FX, transponding")
                )
            .addContent(new Element("programming")
                .setAttribute("direct","byteOnly")
                .setAttribute("paged","yes")
                .setAttribute("register","yes")
                .setAttribute("ops","yes")
                )
            .addContent(new Element("variables")
                .addContent(new Element("variable")
                    .setAttribute("label", "Address")
                    .setAttribute("CV", "1")
                    .setAttribute("minFn", "4")
                    .setAttribute("mask", "VVVVVVVV")
                    .setAttribute("readOnly", "no")
                    .addContent(new Element("decVal")
                        .setAttribute("max", "127")
                        )
                    )
                .addContent(new Element("variable")
                    .setAttribute("label", "Acceleration rate")
                    .setAttribute("CV", "3")
                    .setAttribute("minOut", "2")
                    .setAttribute("mask", "VVVVVVVV")
                    .setAttribute("readOnly", "no")
                    .addContent(new Element("decVal")
                        .setAttribute("max", "127")
                        )
                    )
                .addContent(new Element("variable")
                    .setAttribute("label", "Normal direction of motion")
                    .setAttribute("CV", "29")
                    .setAttribute("minFn", "2")
                    .setAttribute("minOut", "5")
                    .setAttribute("mask", "XXXXXXXV")
                    .setAttribute("readOnly", "no")
                    .addContent(new Element("enumVal")
                        .addContent(new Element("enumChoice")
                            .setAttribute("choice", "forward")
                            )
                        .addContent(new Element("enumChoice")
                            .setAttribute("choice", "reverse")
                            )
                        )
                    )
                )
            )
            ; // end of adding contents

        return;
    }


    // from here down is testing infrastructure

    public DecoderFileTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DecoderFileTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DecoderFileTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

    // static Logger log = Logger.getLogger(DecoderFileTest.class.getName());

}
