package jmri.configurexml;

import java.io.File;
import java.io.IOException;
import jmri.jmrit.XmlFile;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom2.JDOMException;
import org.junit.Assert;

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
     * Does actual validation of a single file.
     *
     * @param file the file to validate
     */
    static public void validate(File file) {
        if (System.getProperty("jmri.skipschematests", "false").equals("true")) {
            return; // skipping check
        }
        System.err.println(SchemaTestBase.class + " validating " + file);
        boolean original = XmlFile.getVerify();
        try {
            XmlFile.setVerify(true);
            XmlFile xf = new XmlFile() {
            };   // odd syntax is due to XmlFile being abstract
            xf.rootFromFile(file);
        } catch (IOException | JDOMException ex) { // throw unexpected errors
            XmlFile.setVerify(original);
            Assert.fail("failed to validate \"" + file.getPath() + "\" due to: " + ex);
        } finally {
            XmlFile.setVerify(original);
        }
    }

    /**
     * Does actual validation of a single file, insisting on failure.
     */
    static void validateFail(File file) {
        if (System.getProperty("jmri.skipschematests", "false").equals("true")) {
            return; // skipping check
        }
        System.err.println(SchemaTestBase.class + " validating " + file);
        boolean original = XmlFile.getVerify();
        try {
            XmlFile.setVerify(true);
            XmlFile xf = new XmlFile() {
            };   // odd syntax is due to XmlFile being abstract
            xf.rootFromFile(file);
            Assert.fail("Validation should have failed: " + file.getName());
        } catch (IOException | JDOMException ex) {
            // expect fail, this is normal
            XmlFile.setVerify(original);
        } finally {
            XmlFile.setVerify(original);
        }
    }

    /**
     * Create the tests that validate an entire directory.
     *
     * @param suite the suite to add tests to
     * @param name  the name of directory
     */
    static public void validateDirectory(TestSuite suite, String name) {
        // adds subsuite even if empty
        TestSuite subsuite = new TestSuite("Directory " + name + " validation");
        suite.addTest(subsuite);

        if (System.getProperty("jmri.skipschematests", "false").equals("true")) {
            return; // skipping check
        }
        File dir = new File(name);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.getName().endsWith(".xml")) {
                subsuite.addTest(new CheckOneFilePasses(file));
            }
        }
    }

    /**
     * Create the tests that validate the subdirectories of a given directory.
     * Not recursive.
     *
     * @param suite the suite to add tests to
     * @param name  the name of the directory
     */
    static public void validateSubdirectories(TestSuite suite, String name) {
        // adds subsuite even if empty
        TestSuite subsuite = new TestSuite("Subdirectories of " + name + " validation");
        suite.addTest(subsuite);

        if (System.getProperty("jmri.skipschematests", "false").equals("true")) {
            return; // skipping check
        }
        File dir = new File(name);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                validateDirectory(subsuite, file.toString());
            }
        }
    }

    /**
     * Create the tests that validate that an entire directory fails. This is
     * used to check that the schema itself is working as a constraint.
     *
     * @param suite the suite to add tests to
     * @param name  the name of the directory
     */
    static public void validateDirectoryFail(TestSuite suite, String name) {
        // adds subsuite even if empty
        TestSuite subsuite = new TestSuite("Directory " + name + " validation");
        suite.addTest(subsuite);

        if (System.getProperty("jmri.skipschematests", "false").equals("true")) {
            return; // skipping check
        }
        File dir = new File(name);
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.getName().endsWith(".xml")) {
                subsuite.addTest(new CheckOneFileFails(file));
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

        @Override
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

        @Override
        public void runTest() {
            validateFail(file);
        }
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
}
