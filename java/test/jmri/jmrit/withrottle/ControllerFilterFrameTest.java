package jmri.jmrit.withrottle;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ControllerFilterFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ControllerFilterFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControllerFilterFrame panel = new ControllerFilterFrame();
        Assert.assertNotNull("exists", panel);
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
