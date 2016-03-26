package jmri.jmrix.ieee802154.xbee;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XBeeConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo
 * class
 *
 * @author	Paul Bender
 */
public class XBeeConnectionMemoTest extends TestCase {

    public void testCtor() {
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        Assert.assertNotNull("exists", m);
    }

    // from here down is testing infrastructure
    public XBeeConnectionMemoTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XBeeConnectionMemoTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XBeeConnectionMemoTest.class);
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
