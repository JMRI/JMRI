package jmri.jmrix.ieee802154.swing.packetgen;

import jmri.InstanceManager;
import jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo;
import jmri.jmrix.ieee802154.IEEE802154TrafficController;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of PacketGenAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class PacketGenActionTest {

    @Test
    public void testIeeeStringMemoCtor() {
        PacketGenAction action = new PacketGenAction("IEEE 802.15.4 test Action", new IEEE802154SystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testMemoCtor() {
        PacketGenAction action = new PacketGenAction( new IEEE802154SystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testStringCtor() {
        IEEE802154SystemConnectionMemo memo = new IEEE802154SystemConnectionMemo();
        new IEEE802154TrafficController(){
            @Override
            protected jmri.jmrix.AbstractMRReply newReply() {
                return null;
            }
            @Override
            public jmri.jmrix.ieee802154.IEEE802154Node newNode() {
                return null;
            }
        };
        InstanceManager.setDefault(IEEE802154SystemConnectionMemo.class, memo);
        PacketGenAction action = new PacketGenAction("IEEE 802.15.4 test Action");
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testDefaultCtor() {
        IEEE802154SystemConnectionMemo memo = new IEEE802154SystemConnectionMemo();
        new IEEE802154TrafficController(){
            @Override
            protected jmri.jmrix.AbstractMRReply newReply() {
                return null;
            }
            @Override
            public jmri.jmrix.ieee802154.IEEE802154Node newNode() {
                return null;
            }
        };
        InstanceManager.setDefault(IEEE802154SystemConnectionMemo.class, memo);
        PacketGenAction action = new PacketGenAction();
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
