package jmri.jmrix.ieee802154.xbee;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * XBeeTurnoutTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeTurnout class
 *
 * @author	Paul Bender
 */
public class XBeeTurnoutTest {

    XBeeTrafficController tc;
    XBeeConnectionMemo memo;

    @Test
    public void testCtor() {
        XBeeTurnout s = new XBeeTurnout("AT1234", "XBee Turnout Test", tc);
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtorAddressPinName() {
        XBeeTurnout s = new XBeeTurnout("AT123:4", "XBee Turnout Test", tc);
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtorAddress2PinName() {
        XBeeTurnout s = new XBeeTurnout("AT123:4:5", "XBee Turnout Test", tc);
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtor16BitHexNodeAddress() {
        XBeeTurnout s = new XBeeTurnout("AT0002:4", "XBee Turnout Test", tc);
        Assert.assertNotNull("exists", s);
    }
    
    @Test
    public void testCtor16BitHexNodeAddress2pin() {
        XBeeTurnout s = new XBeeTurnout("AT0002:4:5", "XBee Turnout Test", tc);
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtor16BitHexStringNodeAddress() {
        XBeeTurnout s = new XBeeTurnout("AT00 02:4", "XBee Turnout Test", tc);
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtor16BitHexStringNodeAddress2pin() {
        XBeeTurnout s = new XBeeTurnout("AT00 02:4:5", "XBee Turnout Test", tc);
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtor64BitHexStringNodeAddress() {
        XBeeTurnout s = new XBeeTurnout("AT00 13 A2 00 40 A0 4D 2D:4", "XBee Turnout Test", tc);
        Assert.assertNotNull("exists", s);
    }

    @Test
    public void testCtor64BitHexStringNodeAddress2pin() {
        XBeeTurnout s = new XBeeTurnout("AT00 13 A2 00 40 A0 4D 2D:4:5", "XBee Turnout Test", tc);
        Assert.assertNotNull("exists", s);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new XBeeInterfaceScaffold();
        memo = new XBeeConnectionMemo();
        memo.setSystemPrefix("A");
        memo.setTurnoutManager(new XBeeTurnoutManager(tc, "A"));
        tc.setAdapterMemo(memo);
    }

    @After
    public void tearDown() {
        tc.terminate();
        jmri.util.JUnitUtil.tearDown();
    }

}
