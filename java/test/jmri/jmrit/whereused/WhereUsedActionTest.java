package jmri.jmrit.whereused;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
