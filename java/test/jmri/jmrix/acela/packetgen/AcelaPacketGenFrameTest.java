package jmri.jmrix.acela.packetgen;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of AcelaPacketGenFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class AcelaPacketGenFrameTest {


    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        AcelaPacketGenFrame f = new AcelaPacketGenFrame(new AcelaSystemConnectionMemo());
        Assert.assertNotNull("exists", f);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
