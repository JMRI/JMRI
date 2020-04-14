package jmri.jmrix.ieee802154.xbee;

import java.beans.PropertyVetoException;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.models.XBee16BitAddress;
import com.digi.xbee.api.models.XBee64BitAddress;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import jmri.Light;

/**
 * XBeeLightManagerTest.java
 * <p>
 * Description:	tests for the jmri.jmrix.ieee802154.xbee.XBeeLightManager class
 *
 * @author	Paul Bender Copyright (C) 2012,2016
 */
public class XBeeLightManagerTest extends jmri.managers.AbstractLightMgrTestBase {

    private XBeeTrafficController tc = null;

    @Override
    public String getSystemName(int i) {
        return "AL2:" + i;
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", l);
    }

    @Test
    @Override
    public void testProvideName() {
        // create
        Light t = l.provide("" + getSystemName(getNumToTest1()));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Test
    public void testProvideIdStringName() {
        // create
        Light t = l.provide("ALNode 1:2");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("correct object returned ", t, l.getBySystemName("ALNode 1:2"));
    }

    @Test
    public void testProvide16BitAddress() {
        // create
        Light t = l.provide("AL00 02:2");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t, l.getBySystemName("AL00 02:2"));
    }

    @Test
    public void testProvide64BitAddress() {
        // create
        Light t = l.provide("AL00 13 A2 00 40 A0 4D 2D:2");
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t, l.getBySystemName("AL00 13 A2 00 40 A0 4D 2D:2"));
    }

    @Override
    @Test
    public void testDefaultSystemName() {
        // create
        Light t = l.provideLight(getSystemName(getNumToTest1()));
        // check
        Assert.assertTrue("real object returned ", t != null);
        Assert.assertEquals("system name correct ", t, l.getBySystemName(getSystemName(getNumToTest1())));
    }

    @Override
    @Test
    public void testUpperLower() {
        Light t = l.provideLight(getSystemName(getNumToTest2()));
        String name = t.getSystemName();
        Assert.assertNull(l.getLight(name.toLowerCase()));
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

    // from here down is testing infrastructure
    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        m.setSystemPrefix("A");
        tc.setAdapterMemo(m);
        m.setTrafficController(tc);
        l = new XBeeLightManager(m);
        m.setLightManager(l);
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

    /**
     * Number of light to test. Made a separate method so it can be overridden
     * in subclasses that do or don't support various numbers
     */
    @Override
    protected int getNumToTest1() {
        return 2;
    }

    @Override
    protected int getNumToTest2() {
        return 7;
    }

    // private final static Logger log = LoggerFactory.getLogger(XBeeLightManagerTest.class);
}
