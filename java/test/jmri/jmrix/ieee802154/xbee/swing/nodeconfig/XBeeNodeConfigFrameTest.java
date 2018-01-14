package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeInterfaceScaffold;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of XBeeNodeConfigFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class XBeeNodeConfigFrameTest {

    private XBeeConnectionMemo m = null;
    private XBeeTrafficController tc = null;
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        XBeeNodeConfigFrame action = new XBeeNodeConfigFrame(tc);
        Assert.assertNotNull("exists", action);
        action.dispose();
    }

    @Test
    public void testInitComponents() throws Exception{
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        XBeeNodeConfigFrame t = new XBeeNodeConfigFrame(tc);
        // for now, just makes ure there isn't an exception.
        t.initComponents();
        t.dispose();
    }

    @Test
    public void testGetTitle(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        XBeeNodeConfigFrame t = new XBeeNodeConfigFrame(tc);
        t.initComponents();
        Assert.assertEquals("title","Configure XBee Nodes",t.getTitle());
        t.dispose();
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new XBeeInterfaceScaffold();
        m = new XBeeConnectionMemo();
        m.setSystemPrefix("ABC");
        tc.setAdapterMemo(m);
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();        tc = null;
    }
}
