// SchemaTest.java

package jmri.configurexml;

import org.apache.log4j.Logger;
import jmri.jmrit.XmlFile;
import java.io.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

//import jmri.InstanceManager;

/**
 * Checks of JMRI XML Schema
 * 
 * @author Bob Jacobsen Copyright 2009
 * @since 2.5.5
 * @version $Revision$
 */
public class SchemaTest extends LoadFileTestBase {

    // the "pass" and "fail" directories contain
    // paired files to test small bits of schema.
    // All the "pass" files should validate;
    // all the "fail" files should not.
    public void testPassFailFiles() {
        // first, passes
        java.io.File dir = new java.io.File("java/test/jmri/configurexml/pass/");
        java.io.File[] files = dir.listFiles();
        for (int i=0; i<files.length; i++) {
            if (files[i].getName().endsWith("xml")) {
                validate(files[i]);
            }
        }
        // 2nd, fails
        dir = new java.io.File("java/test/jmri/configurexml/fail/");
        files = dir.listFiles();
        for (int i=0; i<files.length; i++) {
            if (files[i].getName().endsWith("xml")) {
                validateFail(files[i]);
            }
        }
    }
    
    public void testSampleFiles() {
        java.io.File dir = new java.io.File("java/test/jmri/configurexml/files/");
        java.io.File[] files = dir.listFiles();
        for (int i=0; i<files.length; i++) {
            if (files[i].getName().endsWith("xml")) {
                validate(files[i]);
            }
        }
    }


    void validateFail(File file) {
        boolean original = XmlFile.getVerify();
        try {
            XmlFile.setVerify(true);
            XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
            xf.rootFromFile(file);
            Assert.fail("Validation should have failed: "+file.getName());
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

    static Logger log = Logger.getLogger(SchemaTest.class.getName());
}
