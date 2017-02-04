package jmri.jmrix.acela.packetgen;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

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
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
