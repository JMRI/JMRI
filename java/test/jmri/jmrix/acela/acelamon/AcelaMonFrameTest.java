package jmri.jmrix.acela.acelamon;

import java.awt.GraphicsEnvironment;

import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of AcelaMonFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class AcelaMonFrameTest {


    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        AcelaMonFrame f = new AcelaMonFrame(new AcelaSystemConnectionMemo());
        Assert.assertNotNull("exists", f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {        JUnitUtil.tearDown();    }
}
