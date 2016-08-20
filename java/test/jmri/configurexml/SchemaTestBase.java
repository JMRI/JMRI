package jmri.configurexml;

import jmri.jmrit.XmlFile;
import java.io.*;

import junit.framework.*;

/**
 * Base for XML schema testing
 *
 * @author Bob Jacobsen Copyright 2009, 2014
 * @since 3.9.2
 */
public class SchemaTestBase extends TestCase {

    public SchemaTestBase(String s) {
        super(s);
    }

    /**
     * Does actual validation of a single file
     */
    static public void validate(File file) {
        if (System.getProperty("jmri.skipschematests", "false").equals("true")) return; // skipping check
        boolean original = XmlFile.getVerify();
        try {
            XmlFile.setVerify(true);
            XmlFile xf = new XmlFile() {
            };   // odd syntax is due to XmlFile being abstract
            xf.rootFromFile(file);
        } catch (Exception ex) {
            XmlFile.setVerify(original);
            Assert.fail("failed to validate \"" + file.getPath() + "\" due to: " + ex);
            return;
        } finally {
            XmlFile.setVerify(original);
        }
    }

    /**
     * Does actual validation of a single file, insisting on failure.
     */
    static void validateFail(File file) {
        if (System.getProperty("jmri.skipschematests", "false").equals("true")) return; // skipping check
        boolean original = XmlFile.getVerify();
        try {
            XmlFile.setVerify(true);
            XmlFile xf = new XmlFile() {
            };   // odd syntax is due to XmlFile being abstract
            xf.rootFromFile(file);
            Assert.fail("Validation should have failed: " + file.getName());
        } catch (Exception ex) {
            // expect fail, this is normal
            XmlFile.setVerify(original);
            return;
        } finally {
            XmlFile.setVerify(original);
        }
    }

    /**
     * Create the tests that validate an entire directory
     */
    static public void validateDirectory(TestSuite suite, String name) {
        // adds subsuite even if empty
        TestSuite subsuite = new TestSuite("Directory " + name + " validation");
        suite.addTest(subsuite);

        if (System.getProperty("jmri.skipschematests", "false").equals("true")) return; // skipping check

        java.io.File dir = new java.io.File(name);
        java.io.File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".xml")) {
                subsuite.addTest(new CheckOneFilePasses(files[i]));
            }
        }
    }

    /**
     * Create the tests that validate the subdirectories of a given directory.
     * Not recursive.
     */
    static public void validateSubdirectories(TestSuite suite, String name) {
        // adds subsuite even if empty
        TestSuite subsuite = new TestSuite("Subdirectories of " + name + " validation");
        suite.addTest(subsuite);

        if (System.getProperty("jmri.skipschematests", "false").equals("true")) return; // skipping check

        java.io.File dir = new java.io.File(name);
        java.io.File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                validateDirectory(subsuite, files[i].toString());
            }
        }
    }

    /**
     * Create the tests that validate that an entire directory fails. This is
     * used to check that the schema itself is working as a constraint.
     */
    static public void validateDirectoryFail(TestSuite suite, String name) {
        // adds subsuite even if empty
        TestSuite subsuite = new TestSuite("Directory " + name + " validation");
        suite.addTest(subsuite);

        if (System.getProperty("jmri.skipschematests", "false").equals("true")) return; // skipping check

        java.io.File dir = new java.io.File(name);
        java.io.File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".xml")) {
                subsuite.addTest(new CheckOneFileFails(files[i]));
            }
        }
    }

    /**
     * Internal TestCase class to allow separate tests for every file
     */
    static public class CheckOneFilePasses extends TestCase {

        File file;

        public CheckOneFilePasses(File file) {
            super("Test schema valid: " + file);
            this.file = file;
        }

        public void runTest() {
            validate(file);
        }
    }

    /**
     * Internal TestCase class to allow separate tests for every file that
     * ensure file fails validation
     */
    static public class CheckOneFileFails extends TestCase {

        File file;

        public CheckOneFileFails(File file) {
            super("Test schema invalid: " + file);
            this.file = file;
        }

        public void runTest() {
            validateFail(file);
        }
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
}
