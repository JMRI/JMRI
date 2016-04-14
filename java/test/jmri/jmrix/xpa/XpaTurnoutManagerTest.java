package jmri.jmrix.xpa;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XpaTurnoutManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.xpa.XpaTurnoutManager class
 *
 * @author	Paul Bender
 * @version $Revision: 17977 $
 */
public class XpaTurnoutManagerTest extends TestCase {

    public void testCtor() {
        XpaTurnoutManager t = new XpaTurnoutManager();
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public XpaTurnoutManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XpaTurnoutManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XpaTurnoutManagerTest.class);
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
