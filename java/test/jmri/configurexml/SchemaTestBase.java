// SchemaTestBase.java

package jmri.configurexml;

import jmri.jmrit.XmlFile;
import java.io.*;

import junit.framework.*;

import jmri.util.JUnitUtil;

/**
 * Base for XML schema testing
 * 
 * @author Bob Jacobsen Copyright 2009, 2014
 * @since 3.9.2
 * @version $Revision$
 */
public class SchemaTestBase extends TestCase {

    public SchemaTestBase(String s) {
        super(s);
    }

    /**
     * Does actual validation of a single file
     */
    static public void validate(File file) {
        boolean original = XmlFile.getVerify();
        try {
            XmlFile.setVerify(true);
            XmlFile xf = new XmlFile(){};   // odd syntax is due to XmlFile being abstract
            xf.rootFromFile(file);
        } catch (Exception ex) {
            XmlFile.setVerify(original);
            Assert.fail("failed to validate \""+file.getPath()+"\" due to: "+ex);
            return;
        } finally {
            XmlFile.setVerify(original);
        }
    }

    /**
     * Create the tests that validate an entire directory
     */
    static public void validateDirectory(TestSuite suite, String name) {
        java.io.File dir = new java.io.File(name);
        java.io.File[] files = dir.listFiles();
        if (files == null) return;
        for (int i=0; i<files.length; i++) {
            if (files[i].getName().endsWith(".xml")) {
                suite.addTest(new CheckOneFile(files[i]));
            }
        }
    }

    /**
     * Create the tests that validate the
     * subdirectories of a given directory
     */
    static public void validateSubdirectories(TestSuite suite, String name) {
        java.io.File dir = new java.io.File(name);
        java.io.File[] files = dir.listFiles();
        if (files == null) return;
        for (int i=0; i<files.length; i++) {
            if (files[i].getName().endsWith(".xml")) {
                suite.addTest(new CheckOneFile(files[i]));
            }
        }
    }

    

    /**
     * Internal TestCase class to allow 
     * separate tests for every file
     */
    static public class CheckOneFile extends TestCase {
        File file;
        public CheckOneFile(File file) {
            super("Test schema: "+file);
            this.file = file;
        }
        public void runTest() {
            validate(file);
        }
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception { 
        super.setUp();
        apps.tests.Log4JFixture.setUp(); 
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initMemoryManager();
    }
    
    protected void tearDown() throws Exception { 
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown(); 
    }
}
