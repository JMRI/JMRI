package jmri.jmrix.ieee802154.xbee.swing.packetgen;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.jmrix.ieee802154.xbee.XBeeInterfaceScaffold;
import jmri.InstanceManager;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PacketGenActionTest {

    @Test
    public void testCTor() {
        XBeeTrafficController tc = new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        InstanceManager.store(m,XBeeConnectionMemo.class);

        PacketGenAction t = new PacketGenAction();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testStringCTor() {
        XBeeTrafficController tc = new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        InstanceManager.store(m,XBeeConnectionMemo.class);

        PacketGenAction t = new PacketGenAction("Test Action");
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testStringMemoCTor() {
        XBeeTrafficController tc = new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();

        PacketGenAction t = new PacketGenAction("Test Action",m);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testMemoCTor() {
        XBeeTrafficController tc = new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();

        PacketGenAction t = new PacketGenAction(m);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PacketGenActionTest.class.getName());

}
