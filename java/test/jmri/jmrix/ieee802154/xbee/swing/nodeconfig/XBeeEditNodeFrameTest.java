package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.ieee802154.xbee.XBeeConnectionMemo;
import jmri.jmrix.ieee802154.xbee.XBeeInterfaceScaffold;
import jmri.jmrix.ieee802154.xbee.XBeeNode;
import jmri.jmrix.ieee802154.xbee.XBeeTrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.mockpolicies.Slf4jMockPolicy;
import org.powermock.core.classloader.annotations.MockPolicy;
@MockPolicy(Slf4jMockPolicy.class)

/**
 * Test simple functioning of EditNodeFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class XBeeEditNodeFrameTest {


    private XBeeTrafficController tc = null;
    private XBeeConnectionMemo m = null;
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        XBeeNodeConfigFrame frame = new XBeeNodeConfigFrame(tc);
        XBeeEditNodeFrame action = new XBeeEditNodeFrame(tc,(XBeeNode)(tc.getNodeFromAddress("00 02")),frame);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.resetInstanceManager();
        tc = new XBeeInterfaceScaffold();
        m = new XBeeConnectionMemo();
        m.setSystemPrefix("ABC");
        tc.setAdapterMemo(m);

    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        tc = null;
    }
}
