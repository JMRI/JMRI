package jmri.jmrix.ieee802154.swing.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.ieee802154.IEEE802154TrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of PacketGenFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class PacketGenFrameTest {


    private IEEE802154TrafficController tc = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        PacketGenFrame action = new PacketGenFrame();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new IEEE802154TrafficController() {
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
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();        tc = null;
    }
}
