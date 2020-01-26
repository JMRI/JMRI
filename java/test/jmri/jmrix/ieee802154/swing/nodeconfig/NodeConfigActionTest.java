package jmri.jmrix.ieee802154.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrix.ieee802154.IEEE802154SystemConnectionMemo;
import jmri.jmrix.ieee802154.IEEE802154TrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NodeConfigAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NodeConfigActionTest {

    @Test
    public void testStringMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigAction action = new NodeConfigAction("IEEE 802.15.4 test Action", new IEEE802154SystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigAction action = new NodeConfigAction( new IEEE802154SystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
        NodeConfigAction action = new NodeConfigAction("IEEE 802.15.4 test Action");
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testDefaultCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
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
        NodeConfigAction action = new NodeConfigAction();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
