package jmri.jmrit.decoderdefn;

import javax.swing.JLabel;
import jmri.jmrit.symbolicprog.CvTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import jmri.progdebugger.ProgDebugger;
import jmri.util.JUnitUtil;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * DecoderFileTest.java
 *
 * @author	Bob Jacobsen, Copyright (C) 2001, 2002
 */
public class DecoderFileTest {

    ProgDebugger p = new ProgDebugger();

    @Test
    public void testSingleVersionNumber() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
                "family", "filename", 16, 3, null);
        d.setOneVersion(18);
        Assert.assertEquals("single 18 OK", true, d.isVersion(18));
        Assert.assertEquals("single 19 not OK", false, d.isVersion(19));
    }

    @Test
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

    @Test
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

    @Test
    public void testCtorLow() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "18", null,
                "family", "filename", 16, 3, null);
        Assert.assertEquals("single 17 not OK", false, d.isVersion(17));
        Assert.assertEquals("single 18 OK", true, d.isVersion(18));
        Assert.assertEquals("single 19 not OK", false, d.isVersion(19));
    }

    @Test
    public void testCtorHigh() {
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", null, "18",
                "family", "filename", 16, 3, null);
        Assert.assertEquals("single 17 not OK", false, d.isVersion(17));
        Assert.assertEquals("single 18 OK", true, d.isVersion(18));
        Assert.assertEquals("single 19 not OK", false, d.isVersion(19));
    }

    @Test
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

    @Test
    public void testMfgName() {
        setupDecoder();
        Assert.assertEquals("mfg name ", "Digitrax", DecoderFile.getMfgName(decoder));
    }

    @Test
    public void testLoadTable() {
        setupDecoder();

        // this test should probably be done in terms of a test class instead of the real one...
        JLabel progStatus = new JLabel(" OK ");
        CvTableModel cvModel = new CvTableModel(progStatus, p);
        VariableTableModel variableModel = new VariableTableModel(progStatus,
                new String[]{"Name", "Value"}, cvModel);
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
                "family", "filename", 16, 16, null);

        d.loadVariableModel(decoder, variableModel);
        Assert.assertEquals("read rows ", 3, variableModel.getRowCount());
        Assert.assertEquals("first row name ", "Address", variableModel.getLabel(0));
        Assert.assertEquals("third row name ", "Normal direction of motion", variableModel.getLabel(2));
    }

    @Test
    public void testIncludeCheck() {
        Element e;
        // test some examples
        e = new Element("Test");
        Assert.assertTrue("1 in null", DecoderFile.isIncluded(e, "1", "model", "family", "", ""));

        (e = new Element("Test")).setAttribute("include", "1,2");
        Assert.assertTrue("1 in 1,2", DecoderFile.isIncluded(e, "1", "model", "family", "", ""));
        Assert.assertTrue("2 in 1,2", DecoderFile.isIncluded(e, "2", "model", "family", "", ""));
        Assert.assertTrue("3 in 1,2", !DecoderFile.isIncluded(e, "3", "model", "family", "", ""));

        (e = new Element("Test")).setAttribute("include", "105,205");
        Assert.assertTrue("105 in 105,205", DecoderFile.isIncluded(e, "105", "model", "family", "", ""));

        (e = new Element("Test")).setAttribute("include", "205,105");
        Assert.assertTrue("105 in 205,105", DecoderFile.isIncluded(e, "105", "model", "family", "", ""));

        (e = new Element("Test")).setAttribute("include", "1050,205");
        Assert.assertTrue("105 not in 1050,205", !DecoderFile.isIncluded(e, "105", "model", "family", "", ""));

        (e = new Element("Test")).setAttribute("include", "50,1050");
        Assert.assertTrue("105 not in 50,1050", !DecoderFile.isIncluded(e, "105", "model", "family", "", ""));

        (e = new Element("Test")).setAttribute("include", "827004,827008,827104,827108,827106,828043,828045,828047");
        Assert.assertTrue("827004", DecoderFile.isIncluded(e, "827004", "model", "family", "", ""));
        Assert.assertTrue("Not 827005", !DecoderFile.isIncluded(e, "827005", "model", "family", "", ""));
        Assert.assertTrue("827108", DecoderFile.isIncluded(e, "827108", "model", "family", "", ""));
    }

    @Test
    public void testIncludeCheckRippleDown() {
        Element e;

        // with nothing in element
        e = new Element("Test");
        Assert.assertTrue("1 in 1,2", DecoderFile.isIncluded(e, "1", "model", "family", "1,2", ""));
        Assert.assertTrue("2 in 1,2", DecoderFile.isIncluded(e, "2", "model", "family", "1,2", ""));
        Assert.assertTrue("3 in 1,2", !DecoderFile.isIncluded(e, "3", "model", "family", "1,2", ""));

        // with irrelevant element
        (e = new Element("Test")).setAttribute("include", "4,5");
        Assert.assertTrue("1 in 1,2", DecoderFile.isIncluded(e, "1", "model", "family", "1,2", ""));
        Assert.assertTrue("2 in 1,2", DecoderFile.isIncluded(e, "2", "model", "family", "1,2", ""));
        Assert.assertTrue("3 in 1,2", !DecoderFile.isIncluded(e, "3", "model", "family", "1,2", ""));

        (e = new Element("Test")).setAttribute("include", "105,205");
        Assert.assertTrue("105 in 105,205", DecoderFile.isIncluded(e, "105", "model", "family", "", ""));

        e = new Element("Test");
        Assert.assertTrue("105 in 205,105", DecoderFile.isIncluded(e, "105", "model", "family", "205,105", ""));
        (e = new Element("Test")).setAttribute("include", "1205,1105");
        Assert.assertTrue("105 in 205,105", DecoderFile.isIncluded(e, "105", "model", "family", "205,105", ""));

        e = new Element("Test");
        Assert.assertTrue("105 not in 1050,205", !DecoderFile.isIncluded(e, "105", "model", "family", "1050,205", ""));
        (e = new Element("Test")).setAttribute("include", "222,333");
        Assert.assertTrue("105 not in 1050,205", !DecoderFile.isIncluded(e, "105", "model", "family", "1050,205", ""));

        e = new Element("Test");
        Assert.assertTrue("105 not in 50,1050", !DecoderFile.isIncluded(e, "105", "model", "family", "50,1050", ""));

        e = new Element("Test");
        Assert.assertTrue("827004", DecoderFile.isIncluded(e, "827004", "model", "family", "827004,827008,827104,827108,827106,828043,828045,828047", ""));
        Assert.assertTrue("Not 827005", !DecoderFile.isIncluded(e, "827005", "model", "family", "827004,827008,827104,827108,827106,828043,828045,828047", ""));
        Assert.assertTrue("827108", DecoderFile.isIncluded(e, "827108", "model", "family", "827004,827008,827104,827108,827106,828043,828045,828047", ""));
    }

    @Test
    public void testIncludeCheckModel() {
        Element e;

        // with nothing in element
        e = new Element("Test");
        Assert.assertTrue("1 in model", DecoderFile.isIncluded(e, "1", "model", "family", "model", ""));
        Assert.assertTrue("2 in 1,2,model", DecoderFile.isIncluded(e, "2", "model", "family", "1,2,model", ""));
        Assert.assertTrue("3 in 1,2", !DecoderFile.isIncluded(e, "3", "model", "family", "1,2", ""));

        // with irrelevant element
        (e = new Element("Test")).setAttribute("include", "4,5");
        Assert.assertTrue("1 in model", DecoderFile.isIncluded(e, "1", "model", "family", "model", ""));
        Assert.assertTrue("2 in 1,2,model", DecoderFile.isIncluded(e, "2", "model", "family", "1,2,model", ""));
        Assert.assertTrue("3 in 1,2", !DecoderFile.isIncluded(e, "3", "model", "family", "1,2", ""));
    }

    @Test
    public void testExcludeCheck() {
        Element e;
        // test some examples
        e = new Element("Test");
        Assert.assertTrue("1 in null", DecoderFile.isIncluded(e, "1", "model", "family", "", ""));

        (e = new Element("Test")).setAttribute("exclude", "1,2");
        Assert.assertTrue("1 in 1,2", !DecoderFile.isIncluded(e, "1", "model", "family", "", ""));
        Assert.assertTrue("2 in 1,2", !DecoderFile.isIncluded(e, "2", "model", "family", "", ""));
        Assert.assertTrue("3 in 1,2", DecoderFile.isIncluded(e, "3", "model", "family", "", ""));

        (e = new Element("Test")).setAttribute("exclude", "105,205");
        Assert.assertTrue("105 in 105,205", !DecoderFile.isIncluded(e, "105", "model", "family", "", ""));

        (e = new Element("Test")).setAttribute("exclude", "205,105");
        Assert.assertTrue("105 in 205,105", !DecoderFile.isIncluded(e, "105", "model", "family", "", ""));

        (e = new Element("Test")).setAttribute("exclude", "1050,205");
        Assert.assertTrue("105 not in 1050,205", DecoderFile.isIncluded(e, "105", "model", "family", "", ""));

        (e = new Element("Test")).setAttribute("exclude", "50,1050");
        Assert.assertTrue("105 not in 50,1050", DecoderFile.isIncluded(e, "105", "model", "family", "", ""));

        (e = new Element("Test")).setAttribute("exclude", "827004,827008,827104,827108,827106,828043,828045,828047");
        Assert.assertTrue("827004", !DecoderFile.isIncluded(e, "827004", "model", "family", "", ""));
        Assert.assertTrue("Not 827005", DecoderFile.isIncluded(e, "827005", "model", "family", "", ""));
        Assert.assertTrue("827108", !DecoderFile.isIncluded(e, "827108", "model", "family", "", ""));
    }

    @Test
    public void testExcludeCheckRippleDown() {
        Element e;

        e = new Element("Test");
        Assert.assertTrue("1 in model", !DecoderFile.isIncluded(e, "1", "model", "family", "", "model"));
        Assert.assertTrue("2 in 1,2,model", !DecoderFile.isIncluded(e, "2", "model", "family", "", "1,2,model"));
        Assert.assertTrue("3 in 1,2,model", !DecoderFile.isIncluded(e, "3", "model", "family", "", "1,2,model"));

        (e = new Element("Test")).setAttribute("exclude", "4,5");
        Assert.assertTrue("1 in 1,2", !DecoderFile.isIncluded(e, "1", "model", "family", "", "1,2"));
        Assert.assertTrue("2 in 1,2", !DecoderFile.isIncluded(e, "2", "model", "family", "", "1,2"));
        Assert.assertTrue("3 in 1,2", DecoderFile.isIncluded(e, "3", "model", "family", "", "1,2"));

        e = new Element("Test");
        Assert.assertTrue("105 in 105,205", !DecoderFile.isIncluded(e, "105", "model", "family", "", "105,205"));
        (e = new Element("Test")).setAttribute("exclude", "305,405");
        Assert.assertTrue("105 in 105,205", !DecoderFile.isIncluded(e, "105", "model", "family", "", "105,205"));
    }

    @Test
    public void testExcludeCheckModel() {
        Element e;

        e = new Element("Test");
        Assert.assertTrue("1 in model", !DecoderFile.isIncluded(e, "1", "model", "family", "", "1,2"));
        Assert.assertTrue("2 in 1,2,model", !DecoderFile.isIncluded(e, "2", "model", "family", "", "1,2"));
        Assert.assertTrue("3 in 1,2", DecoderFile.isIncluded(e, "3", "model", "family", "", "1,2"));

        (e = new Element("Test")).setAttribute("exclude", "4,5");
        Assert.assertTrue("1 in model", !DecoderFile.isIncluded(e, "1", "model", "family", "", "1,2"));
        Assert.assertTrue("2 in 1,2,model", !DecoderFile.isIncluded(e, "2", "model", "family", "", "1,2"));
        Assert.assertTrue("3 in 1,2", DecoderFile.isIncluded(e, "3", "model", "family", "", "1,2"));
    }

    @Test
    public void testMinOut() {
        setupDecoder();

        // this test should probably be done in terms of a test class instead of the real one...
        JLabel progStatus = new JLabel(" OK ");
        CvTableModel cvModel = new CvTableModel(progStatus, p);
        VariableTableModel variableModel = new VariableTableModel(progStatus,
                new String[]{"Name", "Value"}, cvModel);
        DecoderFile d = new DecoderFile("mfg", "mfgID", "model", "23", "24",
                "family", "filename", 16, 3, null);

        d.loadVariableModel(decoder, variableModel);
        Assert.assertEquals("read rows ", 2, variableModel.getRowCount());
    }

    @Test
    public void testMinFn() {
        setupDecoder();

        // this test should probably be done in terms of a test class instead of the real one...
        JLabel progStatus = new JLabel(" OK ");
        CvTableModel cvModel = new CvTableModel(progStatus, p);
        VariableTableModel variableModel = new VariableTableModel(progStatus,
                new String[]{"Name", "Value"}, cvModel);
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
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // add some elements
        root.addContent(decoder = new Element("decoder")
                .addContent(new Element("family")
                        .setAttribute("family", "DH142 etc")
                        .setAttribute("mfg", "Digitrax")
                        .setAttribute("defnVersion", "242")
                        .setAttribute("comment", "DH142 decoder: FX, transponding")
                )
                .addContent(new Element("programming")
                        .setAttribute("direct", "byteOnly")
                        .setAttribute("paged", "yes")
                        .setAttribute("register", "yes")
                        .setAttribute("ops", "yes")
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
        ); // end of adding contents

        return;
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DecoderFileTest.class);
}
