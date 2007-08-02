// XmlFileTest.java

package jmri.jmrit;

import java.io.File;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.DocType;

import java.io.InputStream;

/**
 * Tests for the XmlFile class.
 * <P>
 * Uses (creates, modifies, destroys) files in the local preferences directory
 *
 * @author	    Bob Jacobsen  Copyright 2001
 * @version         $Revision: 1.9 $
 */
public class XmlFileTest extends TestCase {

    public void testCheckFile() {
        // XmlFile is abstract, so can't check ctor directly; use local class
        XmlFile x = new XmlFile() {
            };

        // this test uses explicit filenames intentionally, to ensure that
        // the resulting files go into the test tree area.  This is not
        // a test of prefsDir, and shouldn't use that.
        XmlFile.ensurePrefsPresent("prefs");
        XmlFile.ensurePrefsPresent("prefs"+File.separator+"temp");
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

    public void testWriteFile() throws org.jdom.JDOMException, java.io.IOException {
        XmlFile x = new XmlFile() {
            };
        // create a minimal XML file
        Element root = new Element("decoder-config");
        Document doc = new Document(root);
        doc.setDocType(new DocType("decoder-config","decoder-config.dtd"));

        // write it out
        XmlFile.ensurePrefsPresent("prefs");
        XmlFile.ensurePrefsPresent("prefs"+File.separator+"temp");
        File f = new File(XmlFile.prefsDir()+File.separator+"temp"+File.separator+"test.xml");

        x.writeXML(f, doc);
        
        Assert.assertTrue("File expected to be present", f.exists());
    }

    public void testReadFile() throws org.jdom.JDOMException, java.io.IOException {
        // ensure file present
        testWriteFile();
        
        // try to read
        XmlFile x = new XmlFile() {
            };
        Element e = x.rootFromName(XmlFile.prefsDir()+File.separator+"temp"+File.separator+"test.xml");
        Assert.assertTrue("Element found", e!=null);
    }
    
    /**
     * Test error recovery by forcing the 1st attempt to fail (by overriding the
     * method), and capturing the error-notification method
     */
    public void testReadFileMethod1() throws org.jdom.JDOMException, java.io.IOException {
        // ensure file present
        testWriteFile();
        
        // try to read
        testFlag = false;
        XmlFile x = new XmlFile() {
           protected void reportError1(File file, Exception e) {
                log.debug("invoked");
                testFlag = true;
            }
            protected Element getRootViaURI(boolean verify, InputStream stream) throws org.jdom.JDOMException, java.io.FileNotFoundException {
                log.debug("getRootViaURI dummy");
                throw new org.jdom.JDOMException("test dummy");
            }
        };
        Element e = x.rootFromName(XmlFile.prefsDir()+File.separator+"temp"+File.separator+"test.xml");
        log.debug("returns "+testFlag);
        Assert.assertTrue("Error handler invoked OK", testFlag);
    }
    boolean testFlag = false;

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

    // protected access for subclass
    static protected org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XmlFileTest.class.getName());

}
