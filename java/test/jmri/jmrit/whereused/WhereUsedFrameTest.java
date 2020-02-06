package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the WhereUsedFrame Class
 * @author Dave Sand Copyright (C) 2020
 */
public class WhereUsedFrameTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        WhereUsedFrame frame = new WhereUsedFrame();
        Assert.assertNotNull("exists", frame);
        JUnitUtil.dispose(frame);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public  void tearDown() {
        JUnitUtil.tearDown();
    }
}
