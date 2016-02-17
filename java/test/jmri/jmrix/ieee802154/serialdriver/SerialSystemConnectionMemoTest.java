package jmri.jmrix.ieee802154.serialdriver;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SerialSystemConnectionMemoTest.java
 *
 * Description:	tests for the
 * jmri.jmrix.ieee802154.serialdriver.SerialSystemConnectionMemo class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class SerialSystemConnectionMemoTest extends TestCase {

    public void testCtor() {
        SerialSystemConnectionMemo m = new SerialSystemConnectionMemo();
        Assert.assertNotNull("exists", m);
    }

    // from here down is testing infrastructure
    public SerialSystemConnectionMemoTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialSystemConnectionMemoTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialSystemConnectionMemoTest.class);
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
