package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import jmri.util.JUnitUtil;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.jmrix.ieee802154.xbee.XBeeNode;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeInterfaceScaffold;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of EditNodeFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class EditNodeFrameTest {


    private XBeeTrafficController tc = null;
    private XBeeConnectionMemo m = null;
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        NodeConfigFrame frame = new NodeConfigFrame(tc);
        EditNodeFrame action = new EditNodeFrame(tc,(XBeeNode)(tc.getNodeFromAddress("00 02")),frame);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new XBeeInterfaceScaffold();
        m = new XBeeConnectionMemo();
        m.setSystemPrefix("ABC");
        tc.setAdapterMemo(m);

    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
        tc = null;
    }
}
