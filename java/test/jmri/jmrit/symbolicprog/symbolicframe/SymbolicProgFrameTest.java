package jmri.jmrit.symbolicprog.symbolicframe;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of SymbolicProgFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SymbolicProgFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SymbolicProgFrame action = new SymbolicProgFrame();
        Assert.assertNotNull("exists", action);
        JUnitUtil.dispose(action);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
