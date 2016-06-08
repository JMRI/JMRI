package jmri.configurexml;

import junit.framework.Test;
import junit.framework.TestSuite;

//import jmri.InstanceManager;
/**
 * Checks of JMRI XML Schema
 *
 * @author Bob Jacobsen Copyright 2009
 * @since 2.5.5
 */
public class SchemaTest extends jmri.configurexml.SchemaTestBase {

    // from here down is testing infrastructure
    public SchemaTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SchemaTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.configurexml.SchemaTest");

        // the following are just tested for schema pass/fail, not load/store
        validateDirectory(suite, "java/test/jmri/configurexml/valid");
        validateDirectoryFail(suite, "java/test/jmri/configurexml/invalid");

        // also tested for load/store
        validateDirectory(suite, "java/test/jmri/configurexml/load/");

        return suite;
    }
}
