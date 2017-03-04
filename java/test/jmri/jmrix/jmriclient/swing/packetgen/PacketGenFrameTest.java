package jmri.jmrix.jmriclient.swing.packetgen;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.jmriclient.JMRIClientTrafficController;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

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
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        tc = new JMRIClientTrafficController();
    } 

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
        tc = null;
    }
}
