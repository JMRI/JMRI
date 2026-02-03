package jmri.jmrit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.*;

import jmri.util.FileUtil;
import jmri.util.JUnitUtil;

import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.junit.jupiter.api.*;

/**
 * Tests for the XmlFile class.
 * <p>
 * Uses (creates, modifies, destroys) files in the local preferences directory
 * and the custom <user.home>/temp/xml directory
 *
 * @author Bob Jacobsen Copyright 2001
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

                    boolean result;

                    try {
                        XmlFile xf = new XmlFile() {};   // odd syntax is due to XmlFile being abstract
                        xf.setValidate(validate);
                        xf.rootFromInputStream(new java.io.ByteArrayInputStream(content.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
                        result = true;
                    } catch (IOException | JDOMException ex) {
                        result = false;
                        log.debug("result false: ", ex);
                    }

                    log.debug("DTD: {} SCHEMA: {} ({}) expects {} was {}{}", theDTD, theSchema, validate, passes, result, passes != result ? " !!!!!!!!!!!!!!!!!!!!!!!!!" : "");
                    assertEquals( passes, result,
                        () -> "DTD: "+theDTD+" SCHEMA: "+theSchema+" ("+validate+")");

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
        assertDoesNotThrow( () ->  {
            XmlFile xf = new XmlFile() {
            };   // odd syntax is due to XmlFile being abstract
            xf.setValidate(XmlFile.Validate.CheckDtd);
            xf.rootFromFile(file);
        });
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
        assertTrue( x.checkFile("decoders"), "existing file ");  // should be in xml
        assertFalse( x.checkFile("dummy file not expected to exist"), "non-existing file ");
    }

    @Test
    public void testNotVoid() throws JDOMException, IOException {
        // XmlFile is abstract, so can't check ctor directly; use local class
        XmlFile x = new XmlFile() {
        };
        // get Element from non-existant file
        FileNotFoundException e = assertThrows( FileNotFoundException.class,
            () -> x.rootFromFile(new File("nothingwerelikelytofind.xml")));
        assertNotNull(e, "Throws if file not found");
    }

    @Test
    public void testWriteFile() throws IOException {
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

        assertTrue( f.exists(), "File expected to be present");
    }

    @Test
    public void testReadFile() throws JDOMException, IOException {
        // ensure file present
        testWriteFile();

        // try to read
        XmlFile x = new XmlFile() {
        };
        
        // not a real file
        x.setValidate(XmlFile.Validate.None);
        
        Element e = x.rootFromName("temp" + File.separator + "prefs" + File.separator + "test.xml");
        assertNotNull( e, "Element found");
    }

    @Test
    public void testProcessPI() throws JDOMException, IOException {
        // Document from test file
        Document doc;
        Element e;
        try (FileInputStream fs = new FileInputStream(new File("java/test/jmri/jmrit/XmlFileTest_PI.xml"))) {
            SAXBuilder builder = XmlFile.getBuilder(XmlFile.Validate.None);  // argument controls validation
            doc = builder.build(new BufferedInputStream(fs));
            assertNotNull( doc, "Original Document found");
            e = doc.getRootElement();
            assertNotNull( e, "Original root element found");

            XmlFile x = new XmlFile() {
            };
            Document d = x.processInstructions(doc);
            assertNotNull(d);

            // test transform changes <contains> element to <content>
            e = d.getRootElement();
            assertNotNull( e, "Transformed root element found");
            assertEquals( "top", e.getName(), "Transformed root element is right type");
            assertNull( e.getChild("contains"), "Old element gone");
            assertNotNull( e.getChild("content"), "New element there");
            assertEquals( 2, e.getChild("content").getChildren().size(), "New element has content");

        }

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XmlFileTest.class);

}
