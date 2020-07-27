package jmri.jmrix.maple.serialmon;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.jmrix.maple.MapleSystemConnectionMemo;

/**
 * Test simple functioning of SerialMonFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SerialMonFrameTest {

    @Test
    public void testMemoCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SerialMonFrame action = new SerialMonFrame(new MapleSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        
        JUnitUtil.tearDown();
    }
}
