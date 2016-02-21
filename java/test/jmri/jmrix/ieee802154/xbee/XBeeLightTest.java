package jmri.jmrix.ieee802154.xbee;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * XBeeLightTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeLight class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class XBeeLightTest extends TestCase {

    XBeeTrafficController tc;
    XBeeConnectionMemo memo;

    public void testCtor() {
        memo.setSystemPrefix("ABC");
        memo.setLightManager(new XBeeLightManager(tc, "ABC"));
        tc.setAdapterMemo(memo);
        XBeeLight s = new XBeeLight("ABCL1234", "XBee Light Test", tc);
        Assert.assertNotNull("exists", s);
    }

    public void testCtorEncoderPinName() {
        memo.setSystemPrefix("ABC");
        memo.setLightManager(new XBeeLightManager(tc, "ABC"));
        tc.setAdapterMemo(memo);
        XBeeLight s = new XBeeLight("ABCL123:4", "XBee Light Test", tc);
        Assert.assertNotNull("exists", s);
    }

    public void testCtorHexNodeAddress() {
        memo.setSystemPrefix("ABC");
        memo.setLightManager(new XBeeLightManager(tc, "ABC"));
        tc.setAdapterMemo(memo);
        XBeeLight s = new XBeeLight("ABCLABCD:4", "XBee Light Test", tc);
        Assert.assertNotNull("exists", s);
    }

    // from here down is testing infrastructure
    public XBeeLightTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XBeeLightTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XBeeLightTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        tc = new XBeeTrafficController() {
            public void setInstance() {
            }
        };
        memo = new XBeeConnectionMemo();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
