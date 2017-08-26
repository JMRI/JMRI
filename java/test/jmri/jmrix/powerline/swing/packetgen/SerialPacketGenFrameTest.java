package jmri.jmrix.powerline.swing.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.powerline.SerialTrafficControlScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SerialPacketGenFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialPacketGenFrameTest {


    private SerialTrafficControlScaffold tc = null;

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        SerialPacketGenFrame action = new SerialPacketGenFrame(tc);
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new SerialTrafficControlScaffold();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();        tc = null;
    }
}
