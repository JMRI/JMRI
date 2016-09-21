package jmri.jmrit.powerpanel;

import java.util.ResourceBundle;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.PowerPanel package
 *
 * @author	Bob Jacobsen
 */
public class PackageTest extends TestCase {

    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle");

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.powerpanel.PackageTest"); // no tests in class itself
        suite.addTest(jmri.jmrit.powerpanel.PowerPaneTest.suite());
        return suite;
    }

}
