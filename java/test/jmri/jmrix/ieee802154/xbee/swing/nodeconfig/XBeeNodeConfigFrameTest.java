package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeInterfaceScaffold;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of XBeeNodeConfigFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class XBeeNodeConfigFrameTest extends jmri.util.JmriJFrameTestBase {

    private XBeeConnectionMemo m = null;
    private XBeeTrafficController tc = null;

    @Test
    public void testGetTitle(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        frame.initComponents();
        Assert.assertEquals("title","Configure XBee Nodes",frame.getTitle());
    }

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        tc = new XBeeInterfaceScaffold();
        m = new XBeeConnectionMemo();
        m.setSystemPrefix("ABC");
        tc.setAdapterMemo(m);
        if(!GraphicsEnvironment.isHeadless()){
           frame = new XBeeNodeConfigFrame(tc);
        }
    }

    @After
    @Override
    public void tearDown() {        
        tc = null;
        m = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
