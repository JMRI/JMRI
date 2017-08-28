package jmri.jmrix.jmriclient.swing.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.jmriclient.JMRIClientTrafficController;
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


    private JMRIClientTrafficController tc = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        PacketGenFrame action = new PacketGenFrame();
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new JMRIClientTrafficController();
    } 

    @After
    public void tearDown() {        JUnitUtil.tearDown();        tc = null;
    }
}
