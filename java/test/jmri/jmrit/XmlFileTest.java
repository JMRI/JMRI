package jmri.jmrit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import jmri.util.FileUtil;
import jmri.util.JUnitUtil;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the XmlFile class.
 * <p>
 * Uses (creates, modifies, destroys) files in the local preferences directory
 * and the custom <user.home>/temp/xml directory
 *
 * @author	Bob Jacobsen Copyright 2001
 */
public class XmlFileTest {

    // file urls are relative to the 
    // program directory
    final static String testFileDir = "java" + File.separator
            + "test" + File.separator
            + "jmri" + File.separator
            + "util" + File.separator
            + "xml" + File.separator;

    // Test cases to make sure schema and DTD validation 
    // is properly controlled.  For each of DTD valid, invalid and absent
    // (ditto schema), check with validate on and off that proper result is obtained.
    // That's 3*3*2*2 cases!
    enum Type { ABSENT, VALID, INVALID }

    @Test
    public void testValidationControl() {
    
        final String docTypeValid = "<!DOCTYPE decoderIndex-config SYSTEM \"decoderIndex-config.dtd\">";
        final String docTypeInvalid = "<!DOCTYPE layout-config SYSTEM \"layout-config-2-5-4.dtd\">";

        final String contentSchemaValid = "<decoderIndex-config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://jmri.org/xml/schema/decoder.xsd\">";
        final String contentSchemaInvalid = "<decoderIndex-config xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"http://jmri.org/xml/schema/layout-2-9-6.xsd\">";
        final String contentSchemaAbsent = "<decoderIndex-config>";
        
        final String ending = "<decoderIndex><mfgList nmraListDate=\"\" updated=\"\"><manufacturer mfg=\"\"/></mfgList><familyList><family mfg=\"\" name=\"\" file=\"\"/></familyList></decoderIndex></decoderIndex-config>";

        for (XmlFile.Validate validate : XmlFile.Validate.values()) {
            for (Type theDTD : Type.values()) {
                for (Type theSchema : Type.values()) {
                    boolean passes = true;
                                     
                    boolean checkDTD = (validate == XmlFile.Validate.CheckDtd) || (validate == XmlFile.Validate.CheckDtdThenSchema);
                    boolean checkSchema = (validate == XmlFile.Validate.RequireSchema) || (validate == XmlFile.Validate.CheckDtdThenSchema);
                           
                    if (theSchema == Type.INVALID && checkSchema) passes = false; // Cannot find the declaration of element 'decoderIndex-config'.
                    if (theSchema == Type.ABSENT && checkSchema) passes = false; // Cannot find the declaration of element 'decoderIndex-config'.
                    
                    if (theDTD == Type.INVALID && checkDTD) passes = false; // Document root element "decoderIndex-config", must match DOCTYPE root "layout-config".
                    
                    // but if you're checking both
                    if ( checkDTD  && checkSchema) {
                        // a pass is a pass
                        if (theDTD == Type.VALID) passes = true; 
                        if (theSchema == Type.VALID) passes = true;       
                        
                        // but a DTD fail is a fail
                        if (theDTD == Type.INVALID) passes = false; 
                    }
                    
                    // create input
                    
                    String content = "";
                    
                    if (theDTD == Type.VALID) content += docTypeValid;
                    if (theDTD == Type.INVALID) content += docTypeInvalid;

                    if (theSchema == Type.VALID) content += contentSchemaValid;
                    if (theSchema == Type.INVALID) content += contentSchemaInvalid;
                    if (theSchema == Type.ABSENT) content += contentSchemaAbsent;
                    
                    content += ending;

                    boolean result = false;

                    try {
                        XmlFile xf = new XmlFile() {};   // odd syntax is due to XmlFile being abstract
                        xf.setValidate(validate);
                        xf.rootFromInputStream(new java.io.ByteArrayInputStream(content.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                        result = true;
                    } catch (Exception ex) {
                        result = false;
                        log.debug(ex.toString());
                    }

                    log.debug("DTD: "+theDTD+" SCHEMA: "+theSchema+" ("+validate+") expects "+passes+" was "+result+(passes!=result?" !!!!!!!!!!!!!!!!!!!!!!!!!":"") );
                    Assert.assertEquals("DTD: "+theDTD+" SCHEMA: "+theSchema+" ("+validate+")", passes, result);

                }
            }
        }
    }
    
    @Test
    public void testProgIncludeRelative() {
        validateFileAndDtdAccess(new File(testFileDir + "ProgramMainRelative.xml"));
    }

    @Test
    public void testProgIncludeURL() {
        validateFileAndDtdAccess(new File(testFileDir + "ProgramMainURL.xml"));
    }

    @Test
    public void testDotDotDTD() {
        validateFileAndDtdAccess(new File(testFileDir + "DotDotDTD.xml"));
    }

    @Test
    public void testHttpURL() {
        validateFileAndDtdAccess(new File(testFileDir + "HttpURL.xml"));
    }

    @Test
    public void testJustFilename() {
        validateFileAndDtdAccess(new File(testFileDir + "JustFilename.xml"));
    }

    @Test
    public void testPathname() {
        validateFileAndDtdAccess(new File(testFileDir + "Pathname.xml"));
    }


    private void validateFileAndDtdAccess(File file) {  // don't check Schema so can check DTD
        try {
            XmlFile xf = new XmlFile() {
            };   // odd syntax is due to XmlFile being abstract
            xf.setValidate(XmlFile.Validate.CheckDtd);
            xf.rootFromFile(file);
        } catch (Exception ex) {
            Assert.fail(ex.toString());
            return;
        }
    }

    @Test
    public void testCheckFile() {
        // XmlFile is abstract, so can't check ctor directly; use local class
        XmlFile x = new XmlFile() {
        };

        // this test uses explicit filenames intentionally, to ensure that
        // the resulting files go into the test tree area.  This is not
        // a test of the user's files directory, and shouldn't use that.
        FileUtil.createDirectory("temp" + File.separator + "prefs");
        Assert.assertTrue("existing file ", x.checkFile("decoders"));  // should be in xml
        Assert.assertTrue("non-existing file ", !x.checkFile("dummy file not expected to exist"));
    }

    @Test
    public void testNotVoid() throws org.jdom2.JDOMException, java.io.IOException {
        // XmlFile is abstract, so can't check ctor directly; use local class
        XmlFile x = new XmlFile() {
        };
        // get Element from non-existant file
        try {
            Element e = x.rootFromFile(new File("nothingwerelikelytofind.xml"));
            Assert.assertTrue("Never returns void", e != null);
            Assert.assertTrue("Never returns if file not found", false);
        } catch (java.io.FileNotFoundException e) { /* OK, desired exit */ }
    }

    @Test
    public void testWriteFile() throws java.io.IOException {
        XmlFile x = new XmlFile() {
        };
        // create a minimal XML file
        Element root = new Element("decoder-config");
        root.setAttribute("noNamespaceSchemaLocation",
                "http://jmri.org/xml/schema/decoder.xsd",
                org.jdom2.Namespace.getNamespace("xsi",
                        "http://www.w3.org/2001/XMLSchema-instance"));
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // write it out
        FileUtil.createDirectory("temp" + File.separator + "prefs");
        File f = new File("temp" + File.separator + "prefs" + File.separator + "test.xml");

        x.writeXML(f, doc);

        Assert.assertTrue("File expected to be present", f.exists());
    }

    @Test
    public void testReadFile() throws org.jdom2.JDOMException, java.io.IOException {
        // ensure file present
        testWriteFile();

        // try to read
        XmlFile x = new XmlFile() {
        };
        
        // not a real file
        x.setValidate(XmlFile.Validate.None);
        
        Element e = x.rootFromName("temp" + File.separator + "prefs" + File.separator + "test.xml");
        Assert.assertTrue("Element found", e != null);
    }

    @Test
    public void testProcessPI() throws org.jdom2.JDOMException, java.io.IOException {
        // Document from test file
        Document doc;
        Element e;
        FileInputStream fs = new FileInputStream(new File("java/test/jmri/jmrit/XmlFileTest_PI.xml"));
        try {
            SAXBuilder builder = XmlFile.getBuilder(XmlFile.Validate.None);  // argument controls validation
            doc = builder.build(new BufferedInputStream(fs));
            Assert.assertNotNull("Original Document found", doc);
            e = doc.getRootElement();
            Assert.assertNotNull("Original root element found", e);

            XmlFile x = new XmlFile() {
            };
            Document d = x.processInstructions(doc);
            Assert.assertNotNull(d);

            // test transform changes <contains> element to <content>
            e = d.getRootElement();
            Assert.assertNotNull("Transformed root element found", e);
            Assert.assertTrue("Transformed root element is right type", e.getName().equals("top"));
            Assert.assertTrue("Old element gone", e.getChild("contains") == null);
            Assert.assertTrue("New element there", e.getChild("content") != null);
            Assert.assertTrue("New element has content", e.getChild("content").getChildren().size() == 2);

        } catch (java.io.IOException ex) {
            throw ex;
        } catch (org.jdom2.JDOMException ex) {
            throw ex;
        } finally {
            fs.close();
        }

    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(XmlFileTest.class);

}
