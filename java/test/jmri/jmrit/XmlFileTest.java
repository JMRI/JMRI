package jmri.jmrit;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import jmri.util.FileUtil;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom2.DocType;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;

/**
 * Tests for the XmlFile class.
 * <P>
 * Uses (creates, modifies, destroys) files in the local preferences directory
 * and the custom <user.home>/temp/xml directory
 *
 * @author	Bob Jacobsen Copyright 2001
 */
public class XmlFileTest extends TestCase {

    // file urls are relative to the 
    // program directory
    final static String testFileDir = "java" + File.separator
            + "test" + File.separator
            + "jmri" + File.separator
            + "util" + File.separator
            + "xml" + File.separator;

    public void testProgIncludeRelative() {
        validate(new File(testFileDir + "ProgramMainRelative.xml"));
    }

    public void testProgIncludeURL() {
        validate(new File(testFileDir + "ProgramMainURL.xml"));
    }

    public void testDotDotDTD() {
        validate(new File(testFileDir + "DotDotDTD.xml"));
    }

    public void testHttpURL() {
        validate(new File(testFileDir + "HttpURL.xml"));
    }

    public void testJustFilename() {
        validate(new File(testFileDir + "JustFilename.xml"));
    }

    public void testPathname() {
        validate(new File(testFileDir + "Pathname.xml"));
    }

    public void validate(File file) {
        boolean original = XmlFile.getVerify();
        try {
            XmlFile.setVerify(true);
            XmlFile xf = new XmlFile() {
            };   // odd syntax is due to XmlFile being abstract
            xf.rootFromFile(file);
        } catch (Exception ex) {
            XmlFile.setVerify(original);
            Assert.fail(ex.toString());
            return;
        } finally {
            XmlFile.setVerify(original);
        }
    }

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

    public void testWriteFile() throws java.io.IOException {
        XmlFile x = new XmlFile() {
        };
        // create a minimal XML file
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config", "decoder-config.dtd"));

        // write it out
        FileUtil.createDirectory("temp" + File.separator + "prefs");
        File f = new File("temp" + File.separator + "prefs" + File.separator + "test.xml");

        x.writeXML(f, doc);

        Assert.assertTrue("File expected to be present", f.exists());
    }

    public void testReadFile() throws org.jdom2.JDOMException, java.io.IOException {
        // ensure file present
        testWriteFile();

        // try to read
        XmlFile x = new XmlFile() {
        };
        Element e = x.rootFromName("temp" + File.separator + "prefs" + File.separator + "test.xml");
        Assert.assertTrue("Element found", e != null);
    }

    public void testProcessPI() throws org.jdom2.JDOMException, java.io.IOException {
        // Document from test file
        Document doc;
        Element e;
        FileInputStream fs = new FileInputStream(new File("java/test/jmri/jmrit/XmlFileTest_PI.xml"));
        try {
            SAXBuilder builder = XmlFile.getBuilder(false);  // argument controls validation
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

    // from here down is testing infrastructure
    public XmlFileTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XmlFileTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XmlFileTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    static protected Logger log = LoggerFactory.getLogger(XmlFileTest.class.getName());

}
