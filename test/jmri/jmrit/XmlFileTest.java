// XmlFileTest.java

package jmri.jmrit;

import java.io.*;

import junit.framework.*;

/**
 * Tests for the XmlFile class.
 * <P>
 * Uses (creates, modifies, destroys) files in the local preferences directory
 *
 * @author	    Bob Jacobsen  Copyright 2001
 * @version         $Revision: 1.5 $
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
    
    // static private org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(XmlFileTest.class.getName());
    
}
