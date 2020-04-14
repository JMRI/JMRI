package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import jmri.jmrix.ieee802154.xbee.XBeeNode;
import jmri.jmrix.ieee802154.xbee.XBeeInterfaceScaffold;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of StreamConfigPane 
 *
 * @author	Paul Bender Copyright (C) 2018
 */
public class StreamConfigPaneTest {

    private XBeeInterfaceScaffold tc = null;
    private XBeeNode node = null;

    @Test
    public void testCreatePanel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        StreamConfigPane pane = StreamConfigPane.createPanel(node);
        Assert.assertNotNull("exists", pane);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initConnectionConfigManager();
        tc = new XBeeInterfaceScaffold();
        byte pan[] = {(byte) 0x00, (byte) 0x42};
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node = new XBeeNode(pan,uad,gad);
        node.setTrafficController(tc);
    }

    @After
    public void tearDown() {
        node = null;
        tc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
