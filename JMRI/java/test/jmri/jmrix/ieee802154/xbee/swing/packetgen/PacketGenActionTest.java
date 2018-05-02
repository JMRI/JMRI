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
        
    private XBeeConnectionMemo memo = null;

    @Test
    public void testCTor() {
        PacketGenAction t = new PacketGenAction();
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testStringCTor() {
        PacketGenAction t = new PacketGenAction("Test Action");
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testStringMemoCTor() {
        PacketGenAction t = new PacketGenAction("Test Action",memo);
        Assert.assertNotNull("exists",t);
    }

    @Test
    public void testMemoCTor() {
        PacketGenAction t = new PacketGenAction(memo);
        Assert.assertNotNull("exists",t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp(); 
        memo = new XBeeConnectionMemo();
        memo.setTrafficController(new XBeeInterfaceScaffold());
        InstanceManager.store(memo,XBeeConnectionMemo.class);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown(); 
    }

    // private final static Logger log = LoggerFactory.getLogger(PacketGenActionTest.class);

}
