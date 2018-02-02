package jmri.jmrix.ieee802154.xbee;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;
import jmri.Turnout;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * XBeeTurnoutManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeTurnoutManager
 * class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class XBeeTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    XBeeTrafficController tc = null;

    @Override
    public String getSystemName(int i){
       return "AT2:" +i;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout("AT2:" + getNumToTest1());
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertTrue("system name correct ", t == l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testUpperLower() {
        Turnout t = l.provideTurnout("AT2:" + getNumToTest2());

        Assert.assertNull(l.getTurnout(t.getSystemName().toLowerCase()));
    }


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        m.setSystemPrefix("A");
        tc.setAdapterMemo(m);
        l = new XBeeTurnoutManager(tc, "A");
        m.setTurnoutManager(l);
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x00, (byte) 0x02};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan,uad,gad);
        RemoteXBeeDevice rd = new RemoteXBeeDevice(tc.getXBee(),
             new XBee64BitAddress("0013A20040A04D2D"),
             new XBee16BitAddress("0002"),
             "Node 1");
        node.setXBee(rd);
        tc.registerNode(node);
    }

    @After
    public void tearDown() {
        tc.terminate();
        jmri.util.JUnitUtil.tearDown();
    }

}
