// SchemaTest.java

package jmri.configurexml;

import jmri.jmrit.XmlFile;
import java.io.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

import jmri.InstanceManager;

/**
 * Checks of JMRI XML Schema
 * 
 * @author Bob Jacobsen Copyright 2009
 * @since 2.5.5
 * @version $Revision: 1.1 $
 */
public class SchemaTest extends LoadFileTestBase {

    public void testValidateRef() {
        validate(new java.io.File("java/test/jmri/configurexml/JMRItypesTest.xml"));
    }

    public void testValidateFail() {
        java.io.File file = 
                new java.io.File("java/test/jmri/configurexml/SchemaFail.xml");
        
        boolean original = XmlFile.getVerify();
        try {
            XmlFile.setVerify(true);
            XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
            xf.rootFromFile(file);
            Assert.fail("Validation should have failed");
        } catch (Exception ex) {
            // expect fail, this is normal
            XmlFile.setVerify(original);
            return;
        } finally {
            XmlFile.setVerify(original);
        }
    }
    // from here down is testing infrastructure

    public SchemaTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
		String[] testCaseName = {"-noloading", SchemaTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SchemaTest.class);
        return suite;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SchemaTest.class.getName());
}
