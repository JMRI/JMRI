package jmri.jmrix.cmri.serial.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
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

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialPacketGenFrame action = new SerialPacketGenFrame(new CMRISystemConnectionMemo()); 
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
