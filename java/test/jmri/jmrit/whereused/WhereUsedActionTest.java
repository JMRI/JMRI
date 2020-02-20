package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the WhereUsedAction Class
 * @author Dave Sand Copyright (C) 2020
 */
public class WhereUsedActionTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        WhereUsedAction action = new WhereUsedAction();
        Assert.assertNotNull("exists", action);
        action.actionPerformed(null);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
