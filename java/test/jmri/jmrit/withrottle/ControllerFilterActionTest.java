package jmri.jmrit.withrottle;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

/**
 * Test simple functioning of ControllerFilterAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ControllerFilterActionTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControllerFilterAction panel = new ControllerFilterAction();
        Assert.assertNotNull("exists", panel);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
