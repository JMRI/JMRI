package jmri.jmrix.secsi.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Test simple functioning of SerialPacketGenFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialPacketGenFrameTest {

    private SecsiSystemConnectionMemo memo = null;

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialPacketGenFrame action = new SerialPacketGenFrame(memo); 
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        memo = new SecsiSystemConnectionMemo();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
