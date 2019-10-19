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
        JUnitUtil.initConnectionConfigManager();
        tc = new XBeeInterfaceScaffold();
        m = new XBeeConnectionMemo();
        m.setSystemPrefix("ABC");
        tc.setAdapterMemo(m);
        if(!GraphicsEnvironment.isHeadless()) {
           parent = new XBeeNodeConfigFrame(tc);
           byte pan[] = {(byte) 0x00, (byte) 0x42};
           byte uad[] = {(byte) 0x6D, (byte) 0x97};
           byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
           XBeeNode node = new XBeeNode(pan,uad,gad){
               @Override
               public String getIdentifier(){ 
                   return "";
               }
           };
           frame = new XBeeEditNodeFrame(tc,node,parent);
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
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        super.tearDown();
    }
}
