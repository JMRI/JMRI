package jmri.jmrix.ieee802154;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.ieee802154 package
 *
 * @author	Paul Bender
 * @version	$Revision$
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.ieee802154.IEEE802154Test");  // no tests in this class itself
        suite.addTest(new TestSuite(IEEE802154MessageTest.class));
        suite.addTest(new TestSuite(IEEE802154ReplyTest.class));
        suite.addTest(new TestSuite(IEEE802154SystemConnectionMemoTest.class));
        suite.addTest(jmri.jmrix.ieee802154.xbee.PackageTest.suite());
        suite.addTest(jmri.jmrix.ieee802154.serialdriver.PackageTest.suite());
        suite.addTest(ActiveFlagTest.suite());
        return suite;
    }

}
