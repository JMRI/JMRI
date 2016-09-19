package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * XBeeLightTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeLight class
 *
 * @author	Paul Bender copyright (C) 2012,2016
 */
@RunWith(PowerMockRunner.class)
public class XBeeLightTest {

    XBeeTrafficController tc;
    XBeeConnectionMemo memo;

    @Test
    @Ignore("needs further setup")
    public void testCtor() {
        XBeeLight s = new XBeeLight("ABCL1234", "XBee Light Test", tc);
        Assert.assertNotNull("exists", s);
    }

    @Test
    @Ignore("needs further setup")
    public void testCtorEncoderPinName() {
        XBeeLight s = new XBeeLight("ABCL123:4", "XBee Light Test", tc);
        Assert.assertNotNull("exists", s);
    }
 
    @Test
    @Ignore("needs further setup")
    public void testCtorHexNodeAddress() {
        XBeeLight s = new XBeeLight("ABCLABCD:4", "XBee Light Test", tc);
        Assert.assertNotNull("exists", s);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        //apps.tests.Log4JFixture.setUp();
        XBeeTrafficController tc = new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        memo.setSystemPrefix("ABC");
        tc.setAdapterMemo(memo);
        memo.setLightManager(new XBeeLightManager(tc, "ABC"));
    }

    @After
    public void tearDown() {
        //apps.tests.Log4JFixture.tearDown();
    }

}
