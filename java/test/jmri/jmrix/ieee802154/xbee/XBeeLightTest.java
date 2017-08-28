package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
import org.powermock.modules.junit4.PowerMockRunner;
@MockPolicy(Slf4jMockPolicy.class)

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
    public void testCtor() {
        XBeeLight s = new XBeeLight("ABCL1234", "XBee Light Test", tc);
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtorEncoderPinName() {
        XBeeLight s = new XBeeLight("ABCL123:4", "XBee Light Test", tc);
        Assert.assertNotNull("exists", s);
    }
 
    @Test
    public void testCtorHexNodeAddress() {
        XBeeLight s = new XBeeLight("ABCLABCD:4", "XBee Light Test", tc);
        Assert.assertNotNull("exists", s);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        tc = new XBeeInterfaceScaffold();
        memo = new XBeeConnectionMemo();
        memo.setSystemPrefix("ABC");
        tc.setAdapterMemo(memo);
        memo.setLightManager(new XBeeLightManager(tc, "ABC"));
    }

    @After
    public void tearDown() {
    }

}
