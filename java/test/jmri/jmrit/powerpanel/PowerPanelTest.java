// PowerPanelTest.java
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
public class PowerPanelTest extends TestCase {

    static ResourceBundle res = ResourceBundle.getBundle("jmri.jmrit.powerpanel.PowerPanelBundle");

    // from here down is testing infrastructure
    public PowerPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PowerPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.powerpanel.PowerPanelTest"); // no tests in class itself
        suite.addTest(jmri.jmrit.powerpanel.PowerPaneTest.suite());
        return suite;
    }

}
