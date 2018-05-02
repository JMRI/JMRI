package jmri.jmrix.tmcc.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.tmcc.TmccSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SerialPacketGenFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialPacketGenFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialPacketGenFrame action = new SerialPacketGenFrame(new TmccSystemConnectionMemo("T", "TMCC via Serial"));
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() { JUnitUtil.tearDown(); }

}
