package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of XBeeAddNodeFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class XBeeAddNodeFrameTest extends jmri.util.JmriJFrameTestBase {


    private XBeeTrafficController tc = null;
    private XBeeConnectionMemo m = null;
    private XBeeNodeConfigFrame parent = null;
 
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new XBeeTrafficController() {
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
        if(!GraphicsEnvironment.isHeadless()){
           parent = new XBeeNodeConfigFrame(tc);
           frame = new XBeeAddNodeFrame(tc,parent);
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
