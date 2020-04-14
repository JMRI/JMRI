package jmri.jmrix.ieee802154.xbee;

import java.beans.PropertyVetoException;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.Turnout;

/**
 * Tests for the jmri.jmrix.ieee802154.xbee.XBeeTurnoutManager class.
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class XBeeTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    XBeeTrafficController tc = null;

    @Override
    public String getSystemName(int i) {
        return "AT2:" + i;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    @Test
    @Override
    public void testProvideName() {
        // create
        Turnout t = l.provide("" + (getSystemName(getNumToTest1())));
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testProvideIdStringName() {
        // create
        Turnout t = l.provide("ATNode 1:2");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("correct object returned ", t, l.getBySystemName("ATNode 1:2"));
    }

    @Test
    public void testProvide16BitAddress() {
        // create
        Turnout t = l.provide("AT00 02:2");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name correct ", t, l.getBySystemName("AT00 02:2"));
    }

    @Test
    public void testProvide64BitAddress() {
        // create
        Turnout t = l.provide("AT00 13 A2 00 40 A0 4D 2D:2");
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name correct ", t, l.getBySystemName("AT00 13 A2 00 40 A0 4D 2D:2"));
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Turnout t = l.provideTurnout(getSystemName(getNumToTest1()));
        // check
        Assert.assertNotNull("real object returned ", t);
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testUpperLower() {
        Turnout t = l.provideTurnout(getSystemName(getNumToTest2()));

        Assert.assertNull(l.getTurnout(t.getSystemName().toLowerCase()));
    }

    @Override
    @Test
    public void testRegisterDuplicateSystemName() throws PropertyVetoException, NoSuchFieldException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        testRegisterDuplicateSystemName(l,
                l.makeSystemName("00 02:1"),
                l.makeSystemName("00 02:2"));
    }

    @Override
    @Test
    public void testMakeSystemName() {
        String s = l.makeSystemName("00 02:1");
        Assert.assertNotNull(s);
        Assert.assertFalse(s.isEmpty());
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
        m.setTrafficController(tc);
        l = new XBeeTurnoutManager(m);
        m.setTurnoutManager(l);
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x00, (byte) 0x02};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        XBeeNode node = new XBeeNode(pan, uad, gad);
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
        jmri.util.JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        jmri.util.JUnitUtil.tearDown();

    }

    // private final static Logger log = LoggerFactory.getLogger(XBeeTurnoutManagerTest.class);
}
