// XmlFileTest.java

package jmri.jmrit;

import org.apache.log4j.Logger;
import java.io.File;
import jmri.util.FileUtil;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.DocType;

/**
 * Tests for the XmlFile class.
 * <P>
 * Uses (creates, modifies, destroys) files in the local preferences directory
 * and the custom <user.home>/temp/xml directory
 *
 * @author	    Bob Jacobsen  Copyright 2001
 * @version         $Revision$
 */
public class XmlFileTest extends TestCase {

    // file urls are relative to the 
    // program directory
    final static String testFileDir = "java"+File.separator
                                    +"test"+File.separator
                                    +"jmri"+File.separator
                                    +"util"+File.separator
                                    +"xml"+File.separator;
    
    public void testProgIncludeRelative() {
        validate(new File(testFileDir+"ProgramMainRelative.xml"));
    }
    
    public void testProgIncludeURL() {
        validate(new File(testFileDir+"ProgramMainURL.xml"));
    }
    
    public void testDotDotDTD() {
        validate(new File(testFileDir+"DotDotDTD.xml"));
    }
    
    public void testHttpURL() {
        validate(new File(testFileDir+"HttpURL.xml"));
    }
    
    public void testJustFilename() {
        validate(new File(testFileDir+"JustFilename.xml"));
    }
    
    public void testPathname() {
        validate(new File(testFileDir+"Pathname.xml"));
    }
    
    public void validate(File file) {
        boolean original = XmlFile.getVerify();
        try {
            XmlFile.setVerify(true);
            XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
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
        FileUtil.createDirectory("temp"+File.separator+"prefs");
        Assert.assertTrue("existing file ", x.checkFile("decoders"));  // should be in xml
        Assert.assertTrue("non-existing file ", !x.checkFile("dummy file not expected to exist"));
    }

    public void testNotVoid() throws org.jdom.JDOMException, java.io.IOException {
        // XmlFile is abstract, so can't check ctor directly; use local class
        XmlFile x = new XmlFile() {
            };
        // get Element from non-existant file
        try {
            Element e = x.rootFromFile(new File("nothingwerelikelytofind.xml"));
            Assert.assertTrue("Never returns void", e!=null);
            Assert.assertTrue("Never returns if file not found", false);
        } catch (java.io.FileNotFoundException e) { /* OK, desired exit */ }
    }

    public void testWriteFile() throws java.io.IOException {
        XmlFile x = new XmlFile() {
            };
        // create a minimal XML file
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));

        // write it out
        FileUtil.createDirectory("temp"+File.separator+"prefs");
        File f = new File("temp"+File.separator+"prefs"+File.separator+"test.xml");

        x.writeXML(f, doc);
        
        Assert.assertTrue("File expected to be present", f.exists());
    }

    public void testReadFile() throws org.jdom.JDOMException, java.io.IOException {
        // ensure file present
        testWriteFile();
        
        // try to read
        XmlFile x = new XmlFile() {
            };
        Element e = x.rootFromName("temp"+File.separator+"prefs"+File.separator+"test.xml");
        Assert.assertTrue("Element found", e!=null);
    }
    
    // from here down is testing infrastructure
    
    public XmlFileTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {XmlFileTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XmlFileTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
    
    static protected Logger log = Logger.getLogger(XmlFileTest.class.getName());

}
