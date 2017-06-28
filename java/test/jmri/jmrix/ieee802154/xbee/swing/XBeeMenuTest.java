package jmri.jmrix.ieee802154.xbee.swing;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of XBeeMenu
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class XBeeMenuTest {


    private XBeeTrafficController tc = null;
    private XBeeConnectionMemo m = null;
 
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        XBeeMenu action = new XBeeMenu(m);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new XBeeTrafficController() {
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
        m = new XBeeConnectionMemo();
        m.setSystemPrefix("ABC");
        tc.setAdapterMemo(m);

    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
        tc = null;
    }
}
