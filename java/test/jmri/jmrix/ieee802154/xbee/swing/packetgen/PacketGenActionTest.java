package jmri.jmrix.ieee802154.xbee.swing.packetgen;

import jmri.InstanceManager;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeInterfaceScaffold;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class PacketGenActionTest {

    @Test
    public void testCTor() {
        new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        InstanceManager.store(m,XBeeConnectionMemo.class);

        PacketGenAction t = new PacketGenAction();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testStringCTor() {
        new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();
        InstanceManager.store(m,XBeeConnectionMemo.class);

        PacketGenAction t = new PacketGenAction("Test Action");
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testStringMemoCTor() {
        new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();

        PacketGenAction t = new PacketGenAction("Test Action",m);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testMemoCTor() {
        new XBeeInterfaceScaffold();
        XBeeConnectionMemo m = new XBeeConnectionMemo();

        PacketGenAction t = new PacketGenAction(m);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(PacketGenActionTest.class);

}
