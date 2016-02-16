// SchemaTest.java
package jmri;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Checks of JMRI ml/sample files; here because where else would you put it?
 *
 * @author Bob Jacobsen Copyright 2009, 2016
 * @since 4.3.3
 */
public class SchemaTest extends jmri.configurexml.SchemaTestBase {

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
        TestSuite suite = new TestSuite("jmri.SchemaTest");

        // the following are just tested for schema pass/fail, not load/store
        validateDirectory(suite, "xml/samples");

        return suite;
    }
}
