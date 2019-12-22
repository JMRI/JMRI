package jmri.jmrit.throttle;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of BackgroundPanel
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class BackgroundPanelTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        BackgroundPanel panel = new BackgroundPanel();
        Assert.assertNotNull("exists", panel);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetWindows(false,false);
        JUnitUtil.tearDown();
    }
}
