package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.InstanceManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of NodeConfigAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NodeConfigActionTest {

    @Test
    public void testStringMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigAction action = new NodeConfigAction("IEEE 802.15.4 test Action", new XBeeConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        NodeConfigAction action = new NodeConfigAction( new XBeeConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        XBeeConnectionMemo memo = new XBeeConnectionMemo();
        XBeeTrafficController tc = new XBeeTrafficController(){
            @Override
            public void setInstance() {
            }
            @Override
            protected jmri.jmrix.AbstractMRReply newReply() {
                return null;
            }
            @Override
            public jmri.jmrix.ieee802154.IEEE802154Node newNode() {
                return null;
            }
        };
        InstanceManager.setDefault(XBeeConnectionMemo.class, memo);
        NodeConfigAction action = new NodeConfigAction("IEEE 802.15.4 test Action");
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testDefaultCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        XBeeConnectionMemo memo = new XBeeConnectionMemo();
        XBeeTrafficController tc = new XBeeTrafficController(){
            @Override
            public void setInstance() {
            }
            @Override
            protected jmri.jmrix.AbstractMRReply newReply() {
                return null;
            }
            @Override
            public jmri.jmrix.ieee802154.IEEE802154Node newNode() {
                return null;
            }
        };
        InstanceManager.setDefault(XBeeConnectionMemo.class, memo);
        NodeConfigAction action = new NodeConfigAction();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
