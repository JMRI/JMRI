package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of XBeeAddNodeFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class XBeeAddNodeFrameTest {


    private XBeeTrafficController tc = null;
    private XBeeConnectionMemo m = null;
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        XBeeNodeConfigFrame frame = new XBeeNodeConfigFrame(tc);
        XBeeAddNodeFrame action = new XBeeAddNodeFrame(tc,frame);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
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
    public void tearDown() {        JUnitUtil.tearDown();        tc = null;
    }
}
