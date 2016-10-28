package apps;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test upper level loading of config files
 *
 * @author Bob Jacobsen Copyright 2012
 * @since 2.5.5
 */
public class ValidateConfigFilesTest extends jmri.util.swing.GuiUtilBaseTest {

    public void testDirectory(){
        // dummy test until the base classes are converted.
    }

    // from here down is testing infrastructure
    // Note setup() and teardown are provided from base class, and 
    // need to be invoked if you add methods here
    public ValidateConfigFilesTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", ValidateConfigFilesTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("apps.ValidateConfigFilesTest");
        doDirectory(suite, "xml/config");
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
