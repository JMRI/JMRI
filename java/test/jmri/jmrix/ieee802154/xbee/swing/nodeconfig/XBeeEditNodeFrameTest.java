package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeInterfaceScaffold;
import jmri.jmrix.ieee802154.xbee.XBeeNode;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of EditNodeFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class XBeeEditNodeFrameTest extends jmri.util.JmriJFrameTestBase {

    private XBeeTrafficController tc = null;
    private XBeeConnectionMemo m = null;
    private XBeeNodeConfigFrame parent = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new XBeeInterfaceScaffold();
        m = new XBeeConnectionMemo();
        m.setSystemPrefix("ABC");
        tc.setAdapterMemo(m);
        if(!GraphicsEnvironment.isHeadless()) {
           parent = new XBeeNodeConfigFrame(tc);
           frame = new XBeeEditNodeFrame(tc,(XBeeNode)(tc.getNodeFromAddress("00 02")),parent);
        }
    }

    @After
    @Override
    public void tearDown() {
        tc = null;
        m = null;
        if(parent!=null){
           JUnitUtil.dispose(parent);
        }
        parent = null;
        super.tearDown();
    }
}
