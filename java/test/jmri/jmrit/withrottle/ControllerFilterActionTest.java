package jmri.jmrit.withrottle;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of ControllerFilterAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ControllerFilterActionTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControllerFilterAction panel = new ControllerFilterAction();
        Assert.assertNotNull("exists", panel);
    }

    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
