package jmri.jmrix.ieee802154.xbee;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XBeeMessageTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeMessage class
 *
 * @author	Paul Bender
 */
public class XBeeMessageTest extends TestCase {

    public void testCtor() {
        XBeeMessage m = new XBeeMessage(3);
        Assert.assertEquals("length", 3, m.getNumDataElements());
    }

    // from here down is testing infrastructure
    public XBeeMessageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XBeeMessageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XBeeMessageTest.class);
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
