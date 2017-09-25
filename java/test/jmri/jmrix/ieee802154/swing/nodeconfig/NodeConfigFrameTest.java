package jmri.jmrix.ieee802154.swing.nodeconfig;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.ieee802154.IEEE802154TrafficController;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of NodeConfigFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class NodeConfigFrameTest {


    private IEEE802154TrafficController tc = null;
    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        NodeConfigFrame action = new NodeConfigFrame(tc);
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testInitComponents() throws Exception{
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        NodeConfigFrame t = new NodeConfigFrame(tc);
        // for now, just makes ure there isn't an exception.
        t.initComponents();
        t.dispose();
    }

    @Test
    public void testGetTitle(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        NodeConfigFrame t = new NodeConfigFrame(tc);
        t.initComponents();
        Assert.assertEquals("title","Configure IEEE802154 Nodes",t.getTitle());
        t.dispose();
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
