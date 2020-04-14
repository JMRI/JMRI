package jmri.jmrix.maple.serialmon;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import jmri.jmrix.maple.MapleSystemConnectionMemo;

/**
 * Test simple functioning of SerialMonFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SerialMonFrameTest {

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialMonFrame action = new SerialMonFrame(new MapleSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        
        JUnitUtil.tearDown();
    }
}
