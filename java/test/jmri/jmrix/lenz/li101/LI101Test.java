// LI101Test.java
package jmri.jmrix.lenz.li101;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.li101 package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class LI101Test extends TestCase {

    // from here down is testing infrastructure
    public LI101Test(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LI101Test.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.li101.LI101Test");  // no tests in this class itself
        suite.addTest(new TestSuite(LI101AdapterTest.class));
        return suite;
    }

}
