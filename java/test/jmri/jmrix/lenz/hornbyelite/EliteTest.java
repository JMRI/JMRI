// EliteTest.java
package jmri.jmrix.lenz.hornbyelite;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.hornbyelite package
 *
 * @author Paul Bender
 * @version $Revision$
 */
public class EliteTest extends TestCase {

    // from here down is testing infrastructure
    public EliteTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {EliteTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.hornbyelite.EliteTest");  // no tests in this class itself
        suite.addTest(new TestSuite(HornbyEliteCommandStationTest.class));
        suite.addTest(new TestSuite(EliteAdapterTest.class));
        suite.addTest(new TestSuite(EliteConnectionTypeListTest.class));
        suite.addTest(new TestSuite(EliteXNetInitializationManagerTest.class));
        suite.addTest(new TestSuite(EliteXNetThrottleManagerTest.class));
        suite.addTest(new TestSuite(EliteXNetThrottleTest.class));
        suite.addTest(new TestSuite(EliteXNetTurnoutTest.class));
        suite.addTest(new TestSuite(EliteXNetTurnoutManagerTest.class));
        suite.addTest(new TestSuite(EliteXNetProgrammerTest.class));
        return suite;
    }

}
